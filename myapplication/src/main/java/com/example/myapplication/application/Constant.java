package com.example.myapplication.application;

public final class Constant
{

    public static final class Param
    {

        public static final String KEY_STUDENT_ID = "student.id";
        public static final String KEY_SPLASH_SCREEN_STATE = "splashscreen";
        public static final String KEY_WEEKlYSCHEDULE_WEEKDAY = "weeklyschedule.weekday";
        public static final String KEY_LESSON_NAME = "lessonName.name";
        public static final String KEY_VIEW_TYPE_IS_GRID = "view_type_grid";
        public static final String KEY_BELL_GUID = "bell.bellGuid";
        public static final String KEY_WEEKlYSCHEDULE_CURRENT_TAB = "week.current.tab";
        public static final String KEY_WEEKlYSCHEDULE_TYPE = "week.type";
        public static final String KEY_CHAT_USER_GUID = "chat.user.id";


        //fake
        public static final String KEY_SCHOOL_ID = "school.id";
        public static final String KEY_YEAR = "year.running";
        public static final String KEY_DATE_START = "date.start";
        public static final String KEY_DATE_END = "date.end";
        public static final String KEY_DATE = "date";

    }

    public static final class Preference
    {
        public final static String LESSON = "lessonName";
        public final static String YEAR = "year";
        public final static String REFRESH_TIME = "auto.refresh.time";
        public final static String GENERAL = "general";

        public static final class Keys
        {

            public final static String REFRESH_TIME_DISCIPLINE = "discipline";
            public final static String REFRESH_TIME_ABSENT = "absent";
            public final static String REFRESH_TIME_LESSON = "lessonName";
            public final static String REFRESH_TIME_CURRICULUM = "curriculum";
            public final static String REFRESH_TIME_ENCOURANGEMENT = "persuasive";
            public final static String REFRESH_TIME_CHATUSER = "chatUser";
            //===========================================================================================
            public final static String LESSON_VIEW_TYPE_IS_GRID = "isgrid";
            public final static String LESSON_SCORE_VIEW_TYPE_IS_GRID = "value.isgrid";
            //===========================================================================================
            public static final String GENERAL_SELECTED_STUDENT_ID = "student.selected";
        }
    }

    public static final class NotificationTag
    {

        public static final String LESSON = "samim.lessonName";
        public static final String DISCIPLINE = "samim.discipline";
        public static final String Encouragement = "samim.encouragement";
        public static final String WEEKLYSCHEDULE = "samim.weeklyschedule";
        public static final String ABSENT = "samim.absent";
        public static final String CHAT = "samim.chat";



        public static final String SAMIM_SERVICE = "ir.hfj.samim.service";

    }


    public static final class NotificationId
    {
        public final static int SAMIM_SERVICE = 1000;
        public final static int SAMIM_UPDATE_APP = 2000;

        public static final int DISCIPLINE = 9000 + 1;
        public static final int PERSUASIVE = 9000 + 2;
        public static final int WEEKLYSCHEDULE = 9000 + 3;
        public static final int LESSON = 9000 + 4;
    }


    public static final class DownloadManager
    {
        public static final String ACTION_DOWNLOAD = "download";
        public static final String ACTION_GET_STATE = "state";
        public static final String ACTION_CANCEL = "cancel";


        public static final String PARAM_ID = "id";
        public static final String PARAM_TAG = "tag";
        public static final String PARAM_URL = "url";
        public static final String PARAM_TITLE = "periodTitle";
        public static final String PARAM_DES = "des";

        public static final String PARAM_PROCESS = "process";
        public static final String PARAM_MESSAGE = "message";
        public static final String PARAM_STATE = "state";



    }

}

