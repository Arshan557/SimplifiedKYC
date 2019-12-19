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

}
