package com.directdev.portal.tools.services

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.util.Base64
import com.android.volley.Response
import com.android.volley.toolbox.RequestFuture
import com.directdev.portal.R
import com.directdev.portal.tools.event.PhotoResponseEvent
import com.directdev.portal.tools.event.UpdateErrorEvent
import com.directdev.portal.tools.event.UpdateFailedEvent
import com.directdev.portal.tools.event.UpdateFinishEvent
import com.directdev.portal.tools.helper.GsonHelper
import com.directdev.portal.tools.helper.Pref
import com.directdev.portal.tools.helper.Request
import com.directdev.portal.tools.helper.VolleySingleton
import com.directdev.portal.tools.model.*
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import de.greenrobot.event.EventBus
import io.realm.Realm
import io.realm.RealmObject
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**This uses Kotlin (https://kotlinlang.org/)
 *
 * All data and api request to the server are handled by this service, to update all data we can call
 * UpdateService.all() anywhere in the application. To update one data only, we can call the specific
 * function for that data (eg. UpdateService.schedule()).

 * How this work:
 * 1. We call either the all() function or specific functions(schedule(),exam()...). These are
 * helper functions.

 * 2. The helper function that is called will prepare the intent and set the action(SCHEDULE, EXAM, etc)
 * and then call startService() which will launch the service according to the action specified.
 * A IntentService is a singleton, only one instance of this service will be created, if service already exist
 * and startService() is called, it will be queued. When the service is launched, static field isActive will be
 * set to true.

 * 3. startService() will start this service and then trigger onHandleIntent(). onHandleIntent() will
 * then look at the action that is set on step 2, and launch the appropriate function(handleSchedule(), handleExam())

 * 4. The handle functions(handleExam(), handleSchedule()) will request the data and returns it using
 * future, the data then will be saved into a realm database, if the data failed to be saved (because
 * session expired or other things that is not the data were sent to us), using EventBus, we send an
 * UpdateFailedEvent which then will be captured by our activity to launch a function that handles this error.

 * 5. When everything finished (onHandleIntent()chose which handle function to run, run it, receive data,
 * save data to Realm DB), the service will be destroyed by calling onDestroy(), if multiple call to
 * startService() were made, onHandleIntent() will be called again to serve the queued startService()
 * calls until all call is served, and then onDestroy() is called. onDestroy will call EventBus and
 * sent and UpdateFinishEvent, which will then be used to refresh data on the views. Also, isActive
 * will be set to false.
 */

class UpdateService : IntentService("UpdateService") {
    var photo = " "

    override fun onCreate() {
        isActive = true
        super.onCreate()
    }

    //This get called when startService is called
    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (SCHEDULE == action) {
                handleSchedule()
            } else if (EXAM == action) {
                handleExam()
            } else if (FINANCE == action) {
                handleFinance()
            } else if (GRADES == action) {
                handleGrades()
            } else if (TERMS == action) {
                handleTerms()
            } else if (COURSE == action) {
                handleCourse()
            } else if (ACCOUNT == action) {
                handleAccount()
            } else if (RESOURCES == action) {
                handleResource()
            } else if (PHOTO == action) {
                handlePhoto()
            } else if (GPA == action) {
                handleGPA()
            }
        }
    }

    /**
     * Everything below handles the data request and Manipulation

     * ==================================================================================================================================
     * ===========================================Retrieves Schedules Data===============================================================
     * ==================================================================================================================================
     */

    private fun handleSchedule() {
        //Call the API url, and receives the data as string
        val response = bimayApiCall(getString(R.string.request_schedule))

        //if data returned is not empty
        if (response != "[]") {
            try {
                //Turn the response(Which is a JSONArray) to a list of object(Here it is the Schedule and Dates Objects)
                val schedules = GsonHelper.create().fromJson<List<Schedule>>(response, object : TypeToken<List<Schedule>>() {

                }.type)
                val dates = GsonHelper.create().fromJson<List<Dates>>(response, object : TypeToken<List<Dates>>() {

                }.type)

                //And save the list objects that is return by Gson to realm
                insertToRealm(Schedule::class.java, schedules, dates)

            } catch (e: JsonSyntaxException) {
                isSuccess = false
                dataParsingError("Schedule")
            }

        }
    }

    /**==================================================================================================================================
     * ===========================================Retrieves Exam Date data===============================================================
     * ==================================================================================================================================
     */

    private fun handleExam() {
        val response = bimayApiCall(getString(R.string.request_exam))

        try {
            val exams = GsonHelper.create().fromJson<List<Exam>>(response, object : TypeToken<List<Exam>>() {

            }.type)
            val dates = GsonHelper.create().fromJson<List<Dates>>(response, object : TypeToken<List<Dates>>() {

            }.type)

            insertToRealm(Exam::class.java, exams, dates)
        } catch (e: JsonSyntaxException) {
            dataParsingError("Exam")
        }

    }


    /**==================================================================================================================================
     * ===========================================Retrieves Billing Information==========================================================
     * ==================================================================================================================================
     */

    private fun handleFinance() {
        val data: String
        val response = bimayApiCall(getString(R.string.request_finance))

        try {
            /**
             * Finance data is structured like this {"Status":[*Data that we want*]}. The GSON requires
             * the JSONArray of the *Data that we want* to turn it into a List of object(List).
             * So we have to get the JSONArray out of the "Status" property of the JSONObject
             */
            val finance = JSONObject(response)
            data = finance.getJSONArray("Status").toString()

            val finances = GsonHelper.create().fromJson<List<Finance>>(data, object : TypeToken<List<Finance>>() {

            }.type)
            val dates = GsonHelper.create().fromJson<List<Dates>>(data, object : TypeToken<List<Dates>>() {

            }.type)

            insertToRealm(Finance::class.java, finances, dates)
        } catch (e: JSONException) {
            dataParsingError("Finance")
        }

    }


    /**==================================================================================================================================
     * ===========================================Retrieves Grades & Scores==============================================================
     * ==================================================================================================================================


     * Requesting grades is a bit more complicated, there are one link for each terms, to
     * build the links, we takes the prefix(request_grades string) and add to it the term names. That's
     * why this requires parameter while others doesn't

     * Prefix:
     * https://newbinusmaya.binus.ac.id/services/ci/index.php/scoring/ViewGrade/getStudentScore/

     * Final link that is called:
     * https://newbinusmaya.binus.ac.id/services/ci/index.php/scoring/ViewGrade/getStudentScore/1410
     * https://newbinusmaya.binus.ac.id/services/ci/index.php/scoring/ViewGrade/getStudentScore/1420
     * https://newbinusmaya.binus.ac.id/services/ci/index.php/scoring/ViewGrade/getStudentScore/1430
     * https://newbinusmaya.binus.ac.id/services/ci/index.php/scoring/ViewGrade/getStudentScore/1510

     * 1410 = 2014 Odd Semester
     * 1420 = 2014 Even Semester
     * 1430 = 2014 Short Semester
     * 1510 = 2015 Odd Semester

     * All those final links must be called one by one.
     */
    private fun handleGrades() {
        val realm = Realm.getDefaultInstance()
        try {
            val terms = realm.where(Terms::class.java).findAll()
            realm.beginTransaction()
            realm.clear(Grades::class.java)
            realm.clear(GradesCourse::class.java)
            for (term in terms) {
                val data: String
                val response = bimayApiCall(getString(R.string.request_grades) + term.value)

                val arrays = JSONObject(response).getJSONArray("score")
                for (j in 0..arrays.length() - 1) {
                    arrays.getJSONObject(j).put("STRM", term.value)
                }
                data = arrays.toString()
                val grades = GsonHelper.create().fromJson<List<Grades>>(data, object : TypeToken<List<Grades>>() {

                }.type)
                val course = GsonHelper.create().fromJson<List<GradesCourse>>(data, object : TypeToken<List<GradesCourse>>() {

                }.type)

                realm.copyToRealm(grades)
                realm.copyToRealmOrUpdate(course)
            }
            realm.commitTransaction()


        } catch (e: JSONException) {
            dataParsingError("Grades")
        } finally {
            realm.close()
        }
    }


    /**==================================================================================================================================
     * ===========================================Retrieves Semesters data===============================================================
     * ==================================================================================================================================
     */

    private fun handleTerms() {
        val response = bimayApiCall(getString(R.string.request_terms))

        try {
            val terms = GsonHelper.create().fromJson<List<Terms>>(response, object : TypeToken<List<Terms>>() {

            }.type)
            val realm = Realm.getDefaultInstance()
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(terms)
            realm.commitTransaction()
            realm.close()
        } catch (e: JsonSyntaxException) {
            dataParsingError("Terms")
        }

    }


    /**==================================================================================================================================
     * ===========================================Retrieves Course Data==================================================================
     * ==================================================================================================================================
     */

    private fun handleCourse() {
        val realm = Realm.getDefaultInstance()
        try {
            val terms = realm.where(Terms::class.java).findAll()
            realm.beginTransaction()
            for (term in terms) {
                val course = realm.where(Course::class.java).equalTo("STRM", term.value).findAll()
                if (course.isEmpty()) {
                    val data: String
                    val response = bimayApiCall(getString(R.string.request_course) + term.value)

                    val arrays = JSONObject(response).getJSONArray("Courses")
                    for (j in 0..arrays.length() - 1) {
                        arrays.getJSONObject(j).put("STRM", term.value)
                    }
                    data = arrays.toString()

                    val courses = GsonHelper.create().fromJson<List<Course>>(data, object : TypeToken<List<Course>>() {

                    }.type)

                    realm.copyToRealmOrUpdate(courses)
                }
            }
            realm.commitTransaction()
        } catch (e: JSONException) {
            dataParsingError("Course")
        } finally {
            realm.close()
        }
    }


    /**==================================================================================================================================
     * ===========================================Retrieves Name and Major Data==========================================================
     * ==================================================================================================================================
     */

    private fun handleAccount() {
        val response = bimayApiCall(getString(R.string.request_student_info))

        try {
            val data1 = JSONObject(response)
            val name = data1.getJSONObject("Student").getString("Name")
            val major = data1.getJSONObject("Student").getString("Major")

            Pref.save(this, getString(R.string.resource_account_name), name)
            Pref.save(this, getString(R.string.resource_major), major)

            val photo = data1.getJSONObject("Photo").getString("photo")
            if (Pref.read(this, getString(R.string.resource_small_photo), "") != photo) {
                existNewPhoto = true
                this.photo = photo
            }
        } catch (e: JSONException) {
            dataParsingError("Account")
        }

    }


    /**==================================================================================================================================
     * ===========================================Retrieves Profile Pic data=============================================================
     * ==================================================================================================================================
     */

    private fun handlePhoto() {
        if (existNewPhoto) {
            val response = bimayApiCall(getString(R.string.request_photo))
            try {
                val data = JSONObject(response)
                val toDecode = data.getString("photo")
                val photo = Base64.decode(toDecode, Base64.DEFAULT)
                val fileOutputStream = openFileOutput("acc_photo", Context.MODE_PRIVATE)
                fileOutputStream.write(photo)
                fileOutputStream.close()
                Pref.save(this, getString(R.string.photo_downloaded), 1)
                EventBus.getDefault().post(PhotoResponseEvent())
                Pref.save(this, getString(R.string.resource_small_photo), this.photo)
            } catch (e: Exception) {
                dataParsingError("Photo")
            }

        }
    }


    /**==================================================================================================================================
     * ===========================================Retrieves Main GPA=====================================================================
     * ==================================================================================================================================
     */
    private fun handleGPA() {
        val response = bimayApiCall(getString(R.string.request_dashboard))

        try {
            var data4 = "-.-"
            try {
                data4 = JSONObject(response).getJSONObject("WidgetData").getJSONArray("GPA").getJSONObject(0).getString("GPA").substring(0, 3)
            } catch (e: StringIndexOutOfBoundsException) {
                data4 = "N/A"
            } finally {
                Pref.save(this, getString(R.string.resource_gpa), data4)
            }
        } catch (e: JSONException) {
            dataParsingError("GPA")
        }

    }


    /**==================================================================================================================================
     * ===========================================Retrieves Course Resources data========================================================
     * ==================================================================================================================================
     */

    private fun handleResource() {
        val realm = Realm.getDefaultInstance()
        val courses = realm.where(Course::class.java).findAll()
        var shouldUpdate = false
        for (course in courses) {
            val resources = realm.where(Resource::class.java).equalTo("description", course.courseid).findAll()
            if (resources.isEmpty()) {
                shouldUpdate = true
                break
            }
        }
        if (shouldUpdate) {
            try {
                realm.beginTransaction()
                realm.clear(Resource::class.java)
                for (course in courses) {
                    val data: String
                    val url = (getString(R.string.request_resources)
                    +course.courseid + "/"
                    +course.crsE_ID + "/"
                    +course.strm + "/"
                    +course.ssR_COMPONENT + "/"
                    +course.clasS_NBR)
                    val response = bimayApiCall(url)
                    val `object` = JSONObject(response)
                    val pathArray = `object`.getJSONArray("Path")
                    if (pathArray.length() != 0) {
                        val sessionArray = `object`.getJSONArray("Resources")
                        for (i in 0..pathArray.length() - 1) {
                            pathArray.getJSONObject(i).put("description", course.courseid)
                            for (j in 0..sessionArray.length() - 1) {
                                if (sessionArray.getJSONObject(j).getInt("courseOutlineTopicID") == pathArray.getJSONObject(i).getInt("courseOutlineTopicID")) {
                                    pathArray.getJSONObject(i).put("courseOutlineTopicID", sessionArray.getJSONObject(j).getString("sessionIDNUM"))
                                    break
                                }
                            }
                        }
                        data = pathArray.toString()
                        val resources = GsonHelper.create().fromJson<List<Resource>>(data, object : TypeToken<List<Resource>>() {

                        }.type)
                        realm.copyToRealm(resources)
                    } else {
                        val resource = Resource()
                        resource.description = course.courseid
                        resource.courseOutlineTopicID = "N/A"
                        resource.filename = "N/A"
                        resource.location = "N/A"
                        resource.mediaType = "N/A"
                        resource.mediaTypeId = 5
                        resource.path = "N/A"
                        resource.pathid = "N/A"
                        resource.title = "N/A"
                        realm.copyToRealm(resource)
                    }
                }
                realm.commitTransaction()
            } catch (e: JSONException) {
                dataParsingError("Resource & material")
            } finally {
                realm.close()
            }
        }
    }


    /**==================================================================================================================================
     * ================================== Cleans Up everything When this service is destroyed ===========================================
     * ==================================================================================================================================
     */

    override fun onDestroy() {
        isActive = false

        //Post the UpdateFinishEvent to eventbus when update finish and service is destroyed
        EventBus.getDefault().post(UpdateFinishEvent())
        if (isSuccess) {
            val sdf = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault())
            Pref.save(application, getString(R.string.last_update_pref), sdf.format(Date()))
        }
        super.onDestroy()
    }


    /**==================================================================================================================================
     * ================================================= Helper Functions ===============================================================
     * ==================================================================================================================================
     */

    private fun bimayApiCall(url: String): String {
        val future = RequestFuture.newFuture<String>()
        val queue = VolleySingleton.getInstance(this).queue
        queue.add(Request.create(this, url, future, Response.ErrorListener { error ->
            EventBus.getDefault().post(UpdateErrorEvent(error.toString()))
            stopSelf()
        }))
        try {
            return future.get()
        } catch (e: Exception) {
            return ""
        }

    }

    private fun dataParsingError(name: String) {
        //Post the updateFailedEvent to eventBus when update failed
        EventBus.getDefault().post(UpdateFailedEvent(name))
    }

    private fun <E : RealmObject> insertToRealm(clazz: Class<out RealmObject>, objects: Iterable<E>, date: List<Dates>) {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        realm.clear(clazz)
        realm.copyToRealm(objects)
        realm.copyToRealmOrUpdate(date)
        realm.commitTransaction()
        realm.close()
    }

    companion object {
        private val TAG = "UpdateService"
        private val SCHEDULE = "com.directdev.portal.tools.services.action.SCHEDULE"
        private val EXAM = "com.directdev.portal.tools.services.action.EXAM"
        private val FINANCE = "com.directdev.portal.tools.services.action.FINANCE"
        private val GRADES = "com.directdev.portal.tools.services.action.GRADES"
        private val TERMS = "com.directdev.portal.tools.services.action.TERMS"
        private val COURSE = "com.directdev.portal.tools.services.action.COURSE"
        private val ACCOUNT = "com.directdev.portal.tools.services.action.ACCOUNT"
        private val RESOURCES = "com.directdev.portal.tools.services.action.RESOURCES"
        private val PHOTO = "com.directdev.portal.tools.services.action.PHOTO"
        private val GPA = "com.directdev.portal.tools.services.action.GPA"
        private var isSuccess = true
        var isActive = false
        var existNewPhoto = false

        // Below are helper methods to prepare intents to start this UpdateService service.
        fun all(ctx: Context) {
            val trackUpdates = Pref.read(ctx,"trackUpdates",0)
            if (!isActive) {
                UpdateService.schedule(ctx)
                UpdateService.finance(ctx)
                if(trackUpdates == 0){
                    UpdateService.account(ctx)
                    UpdateService.gpa(ctx)
                    UpdateService.exam(ctx)
                    UpdateService.terms(ctx)
                    UpdateService.finance(ctx)
                    UpdateService.grades(ctx)
                    UpdateService.photo(ctx)
                    UpdateService.course(ctx)
                    Pref.save(ctx, "trackUpdates",1)
                }else if(trackUpdates < 4){
                    UpdateService.exam(ctx)
                    Pref.save(ctx, "trackUpdates",trackUpdates + 1)
                }else if(trackUpdates == 4){
                    UpdateService.account(ctx)
                    UpdateService.terms(ctx)
                    UpdateService.gpa(ctx)
                    UpdateService.grades(ctx)
                    UpdateService.photo(ctx)
                    UpdateService.course(ctx)
                    Pref.save(ctx, "trackUpdates",1)
                }
            }
        }

        fun schedule(ctx: Context) {
            val intent = Intent(ctx, UpdateService::class.java)
            intent.action = SCHEDULE
            ctx.startService(intent)
        }

        fun exam(ctx: Context) {
            val intent = Intent(ctx, UpdateService::class.java)
            intent.action = EXAM
            ctx.startService(intent)
        }

        fun finance(ctx: Context) {
            val intent = Intent(ctx, UpdateService::class.java)
            intent.action = FINANCE
            ctx.startService(intent)
        }

        fun terms(ctx: Context) {
            val intent = Intent(ctx, UpdateService::class.java)
            intent.action = TERMS
            ctx.startService(intent)
        }

        fun grades(ctx: Context) {
            val intent = Intent(ctx, UpdateService::class.java)
            intent.action = GRADES
            ctx.startService(intent)
        }

        fun course(ctx: Context) {
            val intent = Intent(ctx, UpdateService::class.java)
            intent.action = COURSE
            ctx.startService(intent)
        }

        fun account(ctx: Context) {
            val intent = Intent(ctx, UpdateService::class.java)
            intent.action = ACCOUNT
            ctx.startService(intent)
        }

        fun resources(ctx: Context) {
            val intent = Intent(ctx, UpdateService::class.java)
            intent.action = RESOURCES
            ctx.startService(intent)
        }

        fun photo(ctx: Context) {
            val intent = Intent(ctx, UpdateService::class.java)
            intent.action = PHOTO
            ctx.startService(intent)
        }

        fun gpa(ctx: Context) {
            val intent = Intent(ctx, UpdateService::class.java)
            intent.action = GPA
            ctx.startService(intent)
        }
    }
}
