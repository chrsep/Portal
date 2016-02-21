package com.directdev.portal.tools.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Base64;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.directdev.portal.R;
import com.directdev.portal.tools.event.PhotoResponseEvent;
import com.directdev.portal.tools.event.UpdateErrorEvent;
import com.directdev.portal.tools.event.UpdateFailedEvent;
import com.directdev.portal.tools.event.UpdateFinishEvent;
import com.directdev.portal.tools.helper.GsonHelper;
import com.directdev.portal.tools.helper.Pref;
import com.directdev.portal.tools.helper.Request;
import com.directdev.portal.tools.helper.VolleySingleton;
import com.directdev.portal.tools.model.Course;
import com.directdev.portal.tools.model.Dates;
import com.directdev.portal.tools.model.Exam;
import com.directdev.portal.tools.model.Finance;
import com.directdev.portal.tools.model.Grades;
import com.directdev.portal.tools.model.GradesCourse;
import com.directdev.portal.tools.model.Resource;
import com.directdev.portal.tools.model.Schedule;
import com.directdev.portal.tools.model.Terms;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**  All data and api request to the server are handled by this service, to update all data we can call
 *  UpdateService.all() anywhere in the application. To update one data only, we can call the specific
 *  function for that data (eg. UpdateService.schedule()).
 *
 *  How this work:
 *  1. We call either the all() function or specific functions(schedule(),exam()...). These are
 *     helper functions.
 *
 *  2. The helper function that is called will prepare the intent and set the action(SCHEDULE, EXAM)
 *     and then call startService() which will launch the service. Service is a singleton, only one
 *     instance of this service will be created, if service already exist and startService is called
 *     the startService() call will be queued. When the service is launched, static field isActive will
 *     be set to true.
 *
 *  3. startService() will start this service and the trigger onHandleIntent(). onHandleIntent() will
 *     then look and the action that is set on step 2, check which on it's and launch the appropriate
 *     function(handleSchedule(), handleExam())
 *
 *  4. The handle functions(handleExam(), handleSchedule()) will request the data and returns it using
 *     future, the data then will be saved into a realm database, if the data failed to be saved(because
 *     session expired and other things were sent to us), using EventBus, we send an UpdateFailedEvent
 *    which then will be captured by our activity to display the please refresh message.
 *
 *  5. When onHandleIntent() finished (choosing which handle function to run, run it, receive data,
 *     save data to Realm DB), the service will be closed and onDestroy() is called, but if multiple
 *     startService() was called, onHandleIntent() will be called again to serve the queued startService()
 *     calls until all call is served, and then onDestroy() is called. onDestroy will call EventBus and
 *     sent and UpdateFinishEvent, which will then be used to refresh data on the views. Also isActive
 *     will be set to false.
 */

public class UpdateService extends IntentService {
    private static final String TAG = "UpdateService";
    private static final String SCHEDULE = "com.directdev.portal.tools.services.action.SCHEDULE";
    private static final String EXAM = "com.directdev.portal.tools.services.action.EXAM";
    private static final String FINANCE = "com.directdev.portal.tools.services.action.FINANCE";
    private static final String GRADES = "com.directdev.portal.tools.services.action.GRADES";
    private static final String TERMS = "com.directdev.portal.tools.services.action.TERMS";
    private static final String COURSE = "com.directdev.portal.tools.services.action.COURSE";
    private static final String ACCOUNT = "com.directdev.portal.tools.services.action.ACCOUNT";
    private static final String RESOURCES = "com.directdev.portal.tools.services.action.RESOURCES";
    private static final String PHOTO = "com.directdev.portal.tools.services.action.PHOTO";
    private static final String GPA = "com.directdev.portal.tools.services.action.GPA";
    public static boolean isActive = false;
    public static boolean existNewPhoto = false;
    public String photo = " ";


    public UpdateService() {
        super("UpdateService");
    }

    // Below are helper methods to prepare intents to start this UpdateService service.
    public static void all(Context ctx){
        if(!isActive) {
            UpdateService.account(ctx);
            UpdateService.gpa(ctx);
            UpdateService.exam(ctx);
            UpdateService.schedule(ctx);
            UpdateService.terms(ctx);
            UpdateService.finance(ctx);
            UpdateService.grades(ctx);
            UpdateService.photo(ctx);
            UpdateService.course(ctx);
            UpdateService.resources(ctx);
        }
    }

    public static void schedule(Context ctx) {
        Intent intent = new Intent(ctx, UpdateService.class);
        intent.setAction(SCHEDULE);
        ctx.startService(intent);
    }

    public static void exam(Context ctx) {
        Intent intent = new Intent(ctx, UpdateService.class);
        intent.setAction(EXAM);
        ctx.startService(intent);
    }

    public static void finance(Context ctx) {
        Intent intent = new Intent(ctx, UpdateService.class);
        intent.setAction(FINANCE);
        ctx.startService(intent);
    }

    public static void terms(Context ctx) {
        Intent intent = new Intent(ctx, UpdateService.class);
        intent.setAction(TERMS);
        ctx.startService(intent);
    }

    public static void grades(Context ctx) {
        Intent intent = new Intent(ctx, UpdateService.class);
        intent.setAction(GRADES);
        ctx.startService(intent);
    }

    public static void course(Context ctx) {
        Intent intent = new Intent(ctx, UpdateService.class);
        intent.setAction(COURSE);
        ctx.startService(intent);
    }

    public static void account(Context ctx) {
        Intent intent = new Intent(ctx, UpdateService.class);
        intent.setAction(ACCOUNT);
        ctx.startService(intent);
    }

    public static void resources(Context ctx) {
        Intent intent = new Intent(ctx, UpdateService.class);
        intent.setAction(RESOURCES);
        ctx.startService(intent);
    }

    public static void photo(Context ctx) {
        Intent intent = new Intent(ctx, UpdateService.class);
        intent.setAction(PHOTO);
        ctx.startService(intent);
    }

    public static void gpa(Context ctx) {
        Intent intent = new Intent(ctx, UpdateService.class);
        intent.setAction(GPA);
        ctx.startService(intent);
    }

    @Override
    public void onCreate() {
        isActive = true;
        super.onCreate();
    }

    //This get called when startService is called
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (SCHEDULE.equals(action)) {
                handleSchedule();
            } else if (EXAM.equals(action)) {
                handleExam();
            }else if (FINANCE.equals(action)) {
                handleFinance();
            }else if (GRADES.equals(action)) {
                handleGrades();
            }else if (TERMS.equals(action)) {
                handleTerms();
            }else if (COURSE.equals(action)) {
                handleCourse();
            }else if (ACCOUNT.equals(action)) {
                handleAccount();
            }else if (RESOURCES.equals(action)) {
                handleResource();
            }else if (PHOTO.equals(action)) {
                handlePhoto();
            }else if (GPA.equals(action)) {
                handleGPA();
            }
        }
    }

    /**
     * Everything below handles the data request and Manipulation
     *
     * ==================================================================================================================================
     * ===========================================Retrieves Schedules Data===============================================================
     * ==================================================================================================================================
     * **/

    private void handleSchedule() {
        String response = bimayApiCall(getString(R.string.request_schedule));

        if(!response.equals("[]")) {
            try {
                //Turns the response(Which is a JSONArray) to a list of object(Here is Schedule and Dates Objects)
                List<Schedule> schedules = GsonHelper.create().fromJson(response, new TypeToken<List<Schedule>>() {
                }.getType());

                //makeList(response, List<Schedule>);
                List<Dates> dates = GsonHelper.create().fromJson(response, new TypeToken<List<Dates>>() {
                }.getType());

                //Save the objects that is return by Gson to realm
                insertToRealm(Schedule.class, schedules, dates);


            } catch (JsonSyntaxException e) {dataParsingError();}
        }
    }

    /**==================================================================================================================================
     * ===========================================Retrieves Exam Date data===============================================================
     * ==================================================================================================================================**/

    private void handleExam() {
        String response = bimayApiCall(getString(R.string.request_exam));

        try{
            List<Exam> exams = GsonHelper.create().fromJson(response, new TypeToken<List<Exam>>() {
            }.getType());
            List<Dates> dates = GsonHelper.create().fromJson(response, new TypeToken<List<Dates>>() {
            }.getType());

            insertToRealm(Schedule.class, exams, dates);
        }catch (JsonSyntaxException e){dataParsingError();}
    }


    /**==================================================================================================================================
     * ===========================================Retrieves Billing Information==========================================================
     * ==================================================================================================================================**/

    private void handleFinance() {
        String data;
        String response = bimayApiCall(getString(R.string.request_finance));

        try {
            /**
             * Finance data is structured like this {"Status":[*Data that we want*]}. The GSON requires
             * the JSONArray of the *Data that we want* to turn it into a List of object(List<Finance>).
             * So we have to get the JSONArray out of the "Status" property of the JSONObject
             */
            JSONObject finance = new JSONObject(response);
            data = finance.getJSONArray("Status").toString();

            List<Finance> finances = GsonHelper.create().fromJson(data, new TypeToken<List<Finance>>() {
            }.getType());
            List<Dates> dates = GsonHelper.create().fromJson(data, new TypeToken<List<Dates>>() {
            }.getType());

            insertToRealm(Finance.class,finances,dates );

        } catch (JSONException e) {dataParsingError();}
    }


    /**==================================================================================================================================
     * ===========================================Retrieves Grades & Scores==============================================================
     * ==================================================================================================================================
     *
     *
     * Requesting grades is a bit more complicated, there are one link for each terms, to
     * build the links, we takes the prefix(request_grades string) and add to it the term names. That's
     * why this requires parameter while others doesn't
     *
     * Prefix:
     * https://newbinusmaya.binus.ac.id/services/ci/index.php/scoring/ViewGrade/getStudentScore/
     *
     * Final link that is called:
     * https://newbinusmaya.binus.ac.id/services/ci/index.php/scoring/ViewGrade/getStudentScore/1410
     * https://newbinusmaya.binus.ac.id/services/ci/index.php/scoring/ViewGrade/getStudentScore/1420
     * https://newbinusmaya.binus.ac.id/services/ci/index.php/scoring/ViewGrade/getStudentScore/1430
     * https://newbinusmaya.binus.ac.id/services/ci/index.php/scoring/ViewGrade/getStudentScore/1510
     *
     * 1410 = 2014 Odd Semester
     * 1420 = 2014 Even Semester
     * 1430 = 2014 Short Semester
     * 1510 = 2015 Odd Semester
     *
     * All those final links must be called one by one.
     */
    private void handleGrades() {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmResults<Terms> terms = realm.where(Terms.class).findAll();
            realm.beginTransaction();
            realm.clear(Grades.class);
            realm.clear(GradesCourse.class);
            for (Terms term:terms) {
                String data;
                String response = bimayApiCall(getString(R.string.request_grades) + term.getValue());

                JSONArray arrays = new JSONObject(response).getJSONArray("score");
                for(int j = 0 ; j < arrays.length() ; j++){
                    arrays.getJSONObject(j).put("STRM",term.getValue());
                }
                data = arrays.toString();
                List<Grades> grades = GsonHelper.create().fromJson(data, new TypeToken<List<Grades>>() {
                }.getType());
                List<GradesCourse> course = GsonHelper.create().fromJson(data, new TypeToken<List<GradesCourse>>() {
                }.getType());

                realm.copyToRealm(grades);
                realm.copyToRealmOrUpdate(course);
            }
            realm.commitTransaction();


        } catch (JSONException e) {dataParsingError();}finally {realm.close();}
    }


    /**==================================================================================================================================
     * ===========================================Retrieves Semesters data===============================================================
     * ==================================================================================================================================**/

    private void handleTerms() {
        String response = bimayApiCall(getString(R.string.request_terms));

        try {
            List<Terms> terms = GsonHelper.create().fromJson(response, new TypeToken<List<Terms>>() {
            }.getType());
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(terms);
            realm.commitTransaction();
            realm.close();
        } catch (JsonSyntaxException e) {dataParsingError();}
    }


    /**==================================================================================================================================
     * ===========================================Retrieves Course Data==================================================================
     * ==================================================================================================================================**/

    private void handleCourse() {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmResults<Terms> terms = realm.where(Terms.class).findAll();
            realm.beginTransaction();
            for (Terms term:terms) {
                RealmResults<Course> course = realm.where(Course.class).equalTo("STRM",term.getValue()).findAll();
                if(course.isEmpty()){
                    String data;
                    String response = bimayApiCall(getString(R.string.request_course) + term.getValue());

                    JSONArray arrays = new JSONObject(response).getJSONArray("Courses");
                    for(int j = 0 ; j < arrays.length() ; j++){
                        arrays.getJSONObject(j).put("STRM",term.getValue());
                    }
                    data = arrays.toString();

                    List<Course> courses = GsonHelper.create().fromJson(data, new TypeToken<List<Course>>() {
                    }.getType());

                    realm.copyToRealmOrUpdate(courses);
                }
            }
            realm.commitTransaction();
        } catch (JSONException e) {
            dataParsingError();
        }finally {
            realm.close();
        }
    }


    /**==================================================================================================================================
     * ===========================================Retrieves Name and Major Data==========================================================
     * ==================================================================================================================================**/

    private void handleAccount(){
        String response = bimayApiCall(getString(R.string.request_student_info));

        try {
            JSONObject data1 = new JSONObject(response);
            String name = data1.getJSONObject("Student").getString("Name");
            String major = data1.getJSONObject("Student").getString("Major");

            Pref.save(this,getString(R.string.resource_account_name), name);
            Pref.save(this,getString(R.string.resource_major), major);

            String photo = data1.getJSONObject("Photo").getString("photo");
            if (!Pref.read(this, getString(R.string.resource_small_photo), "").equals(photo)) {
                existNewPhoto = true;
                this.photo = photo;
            }
        }catch (JSONException e){
            dataParsingError();
        }
    }


    /**==================================================================================================================================
     * ===========================================Retrieves Profile Pic data=============================================================
     * ==================================================================================================================================**/

    private void handlePhoto(){
        if (existNewPhoto){
            String response = bimayApiCall(getString(R.string.request_photo));
            try {
                JSONObject data = new JSONObject(response);
                String toDecode = data.getString("photo");
                byte[] photo = Base64.decode(toDecode, Base64.DEFAULT);
                FileOutputStream fileOutputStream = openFileOutput("acc_photo", Context.MODE_PRIVATE);
                fileOutputStream.write(photo);
                fileOutputStream.close();
                Pref.save(this, getString(R.string.photo_downloaded), 1);
                EventBus.getDefault().post(new PhotoResponseEvent());
                Pref.save(this,getString(R.string.resource_small_photo), this.photo);
            } catch (Exception e) {
            }
        }
    }


    /**==================================================================================================================================
     * ===========================================Retrieves Main GPA=====================================================================
     * ==================================================================================================================================**/
    private void handleGPA(){
        String response = bimayApiCall(getString(R.string.request_dashboard));

        try {
            String data4 = "-.-";
            try {
                data4 = new JSONObject(response)
                        .getJSONObject("WidgetData")
                        .getJSONArray("GPA")
                        .getJSONObject(0)
                        .getString("GPA")
                        .substring(0, 3);
            } catch (StringIndexOutOfBoundsException e) {
                data4 = "N/A";
            } finally {
                Pref.save(this, getString(R.string.resource_gpa), data4);
            }
        } catch (JSONException e) {
        }
    }


    /**==================================================================================================================================
     * ===========================================Retrieves Course Resources data========================================================
     * ==================================================================================================================================**/

    private void handleResource(){
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Course> courses = realm.where(Course.class).findAll();
        boolean shouldUpdate = false;
        for (Course course:courses) {
            RealmResults<Resource> resources = realm.where(Resource.class).equalTo("description",course.getCOURSEID()).findAll();
            if(resources.isEmpty()){
                shouldUpdate = true;
                break;
            }
        }
        if(shouldUpdate){
            try {
                realm.beginTransaction();
                realm.clear(Resource.class);
                for (Course course:courses) {
                    String data;
                    String url = getString(R.string.request_resources)
                            + course.getCOURSEID() + "/"
                            + course.getCRSE_ID() + "/"
                            + course.getSTRM() + "/"
                            + course.getSSR_COMPONENT() + "/"
                            + course.getCLASS_NBR();
                    String response = bimayApiCall(url);
                    JSONObject object = new JSONObject(response);
                    JSONArray pathArray = object.getJSONArray("Path");
                    if(pathArray.length() != 0){
                        JSONArray sessionArray = object.getJSONArray("Resources");
                        for (int i = 0 ; i < pathArray.length() ; i++){
                            pathArray.getJSONObject(i).put("description",course.getCOURSEID());
                            for (int j = 0; j < sessionArray.length(); j++){
                                if(sessionArray.getJSONObject(j).getInt("courseOutlineTopicID") == pathArray.getJSONObject(i).getInt("courseOutlineTopicID")){
                                    pathArray.getJSONObject(i).put("courseOutlineTopicID",sessionArray.getJSONObject(j).getString("sessionIDNUM"));
                                    break;
                                }
                            }
                        }
                        data = pathArray.toString();
                        List<Resource> resources = GsonHelper.create().fromJson(data, new TypeToken<List<Resource>>() {
                        }.getType());
                        realm.copyToRealm(resources);
                    }else {
                        Resource resource = new Resource();
                        resource.setDescription(course.getCOURSEID());
                        resource.setCourseOutlineTopicID("N/A");
                        resource.setFilename("N/A");
                        resource.setLocation("N/A");
                        resource.setMediaType("N/A");
                        resource.setMediaTypeId(5);
                        resource.setPath("N/A");
                        resource.setPathid("N/A");
                        resource.setTitle("N/A");
                        realm.copyToRealm(resource);
                    }
                }
                realm.commitTransaction();
            } catch(JSONException e){
                dataParsingError();
            }finally {realm.close();}
        }
    }


    /**==================================================================================================================================
     * ================================== Cleans Up everything When this service is destroyed ===========================================
     * ==================================================================================================================================**/

    @Override
    public void onDestroy() {
        isActive = false;

        //Post the UpdateFinishEvent to eventbus when update finish and service is destroyed
        EventBus.getDefault().post(new UpdateFinishEvent());
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        Pref.save(getApplication(),getString(R.string.last_update_pref),sdf.format(new Date()));
        super.onDestroy();
    }



    /**==================================================================================================================================
     * ================================================= Helper Functions ===============================================================
     * ==================================================================================================================================**/

    private String bimayApiCall(String url){
        RequestFuture<String> future = RequestFuture.newFuture();
        RequestQueue queue = VolleySingleton.getInstance(this).getQueue();
        queue.add(Request.create(this, url, future, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                EventBus.getDefault().post(new UpdateErrorEvent(error.toString()));
                stopSelf();
            }
        }));
        try{
            return future.get();
        }catch (Exception e){
            return "";
        }
    }

    private void dataParsingError(){
        //Called when request fails
        stopSelf();

        //Post the updateFailedEvent to eventBus when update failed
        EventBus.getDefault().post(new UpdateFailedEvent());
    }

    private <E extends RealmObject> void insertToRealm(Class<? extends RealmObject> clazz, Iterable<E> objects, List<Dates> date){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.clear(clazz);
        realm.copyToRealm(objects);
        realm.copyToRealmOrUpdate(date);
        realm.commitTransaction();
        realm.close();
    }
}
