package com.calendarfx.app;

import com.calendarfx.model.Interval;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alex Hendrik
 *
 * This class is responsible for conversion of the schedule PDF into a string and processing it to isolate each individual course and assemble the full list of the user's courses.
 * This class implements Apache's PDF Box for simple conversion of a pdf file into a string.
 */
public class Loader implements DepartmentData {

    String content;
    String studentInfo;
    String courseID;
    boolean toggle;
    File schedulePDF;
    Loader loader;

    int char1;
    int char2;
    int char3;
    int char4;

    String userID;
    String userName;

    private String[] courses;
    private int courseIndex;

    public static int index;

    public ArrayList<Course> courseList = new ArrayList<Course>();


    /**
     * This method uses a pattern matcher and substring search to obtain the student ID and full name from the schedule to ensure security of the system.
     * @param string This argument is the first half of the content string that gets passed into the method.
     */
    public void runUserDetect(String string){

        String ID = "\\b" + "\\d{7}" + "\\b";
        Pattern numberPattern = Pattern.compile(ID, Pattern.CASE_INSENSITIVE);
        Matcher matcher = numberPattern.matcher(string);
        if(matcher.find()) {
            this.userID = string.substring(matcher.start(),matcher.end());
        }
        userName = string.substring(string.indexOf("Name:") + 6, string.indexOf("ID"));
    }


    /**
     * This is the primary method responsible for processing the pdf file and splitting it into individual courses.
     * @param schedulePDF This is a file holder which receives the user's schedule PDF from the constructor called by AppManager.java
     */
    public void runDetect(File schedulePDF) {

        loader.courses = new String[20];

        try {
            PDDocument document = PDDocument.load(schedulePDF);
            PDFTextStripper s = new PDFTextStripper();
            loader.content = s.getText(document);
            document.close();

            String[] splitString = loader.content.split("Instructor", 2);

            loader.studentInfo = splitString[0];
            loader.content = splitString[1];

            for (int i = 0; i < departments.length; i++) {
                //find any departments with a \n after them and replace it with a space
                loader.content = loader.content.replaceAll(departmentsFix[i], departments[i]);
            }


            for (String x : departments) {
                if(loader.content.contains(x)) {
                    //see if the schedule contains a particular department class

                    String findStr = x;
                    int lastIndex = 0;
                    int count = 0;
                    while(lastIndex != -1){

                        lastIndex = loader.content.indexOf(findStr,lastIndex);

                        if(lastIndex != -1){
                            count ++;
                            lastIndex += findStr.length();
                        }
                    }
                    //Check how many instances of the department are in the schedule and set the appropriate values

                    setChars(loader, x.length());

                    if(count == 1) {
                        int location = loader.content.indexOf(x);


                        addCourse(loader, x, location);

                    } else if (count > 1) {
                        int location = 0;

                        for(int i = 0; i < count; i++) {
                            //go through the selection routine, i is the instance number for the department
                            //find the first instance, get the ID, then go and get the first instance after the index of the previous one
                            location = loader.content.indexOf(x, location + x.length() * i);

                            if(location > -1) {
                                addCourse(loader, x, location);
                            }
                        }
                    }
                }

            }


            Collections.sort(loader.courseList);

            for (int i = 0; i < loader.courseList.size(); i++) {
                loader.toggle = true;
                index = i;
                loader.courseList.get(i).courseString = loader.content.substring(loader.courseList.get(i).stringLocation, loader.courseList.get(i + 1).stringLocation);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            loader.courseList.get(index).courseString = loader.content;
            loader.content = "";
            System.out.println("----------------------------------------------  " + loader.courseList.get(index).courseID);


        } catch (StringIndexOutOfBoundsException e) {

            loader.courseList.get(index).courseString = loader.content.substring(loader.courseList.get(index).stringLocation);

        } catch (IndexOutOfBoundsException e) {

            loader.courseList.get(index).courseString = loader.content.substring(loader.courseList.get(index).stringLocation);

        } finally {
            for (Course x : loader.courseList){
                for (String y : weekDays) {
                    if (x.courseString.contains(y)) {
                        x.courseDays += y + " ";
                        x.Days.add(y);
                    }
                }
                x.findTime();
            }
        }

        for (Course x : loader.courseList) {

            if (x != null) {
                System.out.println(x.courseID);
                System.out.println(x.courseDays);
                System.out.println(x.courseTime);
                System.out.println("-------------------------------------------------------------------------------------");
            }
        }

    }

    /**
     * This is a method for creating an individual course after runDetect(File) has identified it.
     * @param a Internal reference to the loader object.
     * @param b Internal reference to the schedule string.
     * @param c Internal reference to the string location of the course.
     */
    private static void addCourse(Loader a, String b, int c) {
        Loader loader = a; String x = b; int location = c;
        loader.courseID = Character.toString((loader.content.charAt(location + loader.char1))) +
                Character.toString((loader.content.charAt(location + loader.char2))) +
                Character.toString((loader.content.charAt(location + loader.char3))) +
                Character.toString((loader.content.charAt(location + loader.char4)));

        loader.courses[loader.courseIndex] = x + loader.courseID;

        Course course = new Course(loader.courses[loader.courseIndex], location);

        loader.courseList.add(course);
        loader.courseIndex += 1;
    }

    /**
     * This method adapts the detection algorithm to support department names that have 2-4 letters in them, this prevents the program from reading incomplete
     * @param loader Internal reference to the loader object.
     * @param length Length of the detected department's name
     */
    private static void setChars(Loader loader, int length) {

        loader.char1 = length;  // Covers all cases for the department ID length varying from 2 to 4
        loader.char2 = length + 1;
        loader.char3 = length + 2;
        loader.char4 = length + 3;

        Interval interval = new Interval();

    }

    /**
     * Generic getter for the list of courses.
     * @return List of courses.
     */
    public ArrayList<Course> getCourseList(){
        return courseList;
    }

    /**
     * Generic getter for the user student ID.
     * @return Student ID.
     */
    public String getUserID(){
        return userID;
    }

    /**
     * Generic getter for the full name of the user.
     * @return Full name of the student.
     */
    public String getUserName(){
        return userName;
    }

    /**
     * This is a constructor that creates a new instance of Loader and instantly runs the detection algorithm.
     * @param pdf Input pdf file that must contain the user's schedule.
     */
    public Loader(File pdf){
        schedulePDF = pdf;
        loader = this;
        runDetect(schedulePDF);
        runUserDetect(studentInfo);
    }


    /**
     * This method is used to reset the instance of Loader, thus eliminating the need to create more than one instance and saving memory.
     */
    public void reset(){
        courseList.clear();
        content = "";
        studentInfo = "";
        schedulePDF = null;
        userName = "";
        userID = "";
    }

}
