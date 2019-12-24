package com.techsquad.simplifiedkyc.Utils;

import android.os.Environment;

import java.io.File;

/**
 * Created by Arshan on 11/12/2019.
 */
public class Constants {


        private Constants(){
                //cannot be instantiated
        }

        public static final String SCAN_IMAGE_LOCATION = Environment.getExternalStorageDirectory() + File.separator + "eKYC";
        public static final String SERVICE_URL = "http://192.168.43.60:8080/user/";
        public static final String AADHAR_URL = "http://192.168.43.221:8080/kyc/getAadhar/";
        public static final String PAN_URL = "http://192.168.43.221:8080/kyc/getPAN/";

}
