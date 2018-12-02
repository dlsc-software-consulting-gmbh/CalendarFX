package com.calendarfx.app;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alex Hendrik
 *
 * This class is the generic type for a course that stores all of the related information and is crucial to the program's manipulation of the user's schedule.
 * It contains several constructors that are used for specific purposes.
 */
public class Course implements Comparable<Course> {

    String courseID;
    String courseTime;
    ArrayList<String> Time = new ArrayList<>();
    ArrayList<String> Days = new ArrayList<>();
    String courseDays = "";
    int stringLocation;
    int startTime;

    LocalTime startingTime;
    LocalTime endingTime;

    LocalDate mondayDate = LocalDate.parse("2018-09-03");
    LocalDate tuesdayDate = LocalDate.parse("2018-09-03");;
    LocalDate wednesdayDate = LocalDate.parse("2018-09-03");;
    LocalDate thursdayDate = LocalDate.parse("2018-09-03");;
    LocalDate fridayDate = LocalDate.parse("2018-09-03");;
    LocalDate saturdayDate = LocalDate.parse("2018-09-03");;
    LocalDate sundayDate = LocalDate.parse("2018-09-03");;

    int endTime;
    String courseString;
    int ownerID;


    /**
     * This is a constructor used to make new custom events since the user provides all of the necessary info.
     * @param course This sets the course number to an internal 000 values.
     * @param startTime This is the weekly starting time of the custom event.
     * @param endTime This is the weekly ending time of the custom event.
     * @param days This is the string created from the days selected by the user.
     */
    public Course(String course, String startTime, String endTime, ArrayList<String> days) { //TODO this is for custom event use



        Time.add(startTime);
        Time.add(endTime); //potential exception
        courseID = course;
        courseTime = startTime + " - " + endTime;//redundant
        Days = days;
        startingTime = LocalTime.parse(startTime);
        endingTime = LocalTime.parse(endTime);
    }


    /**
     * This is a constructor used in downloading the courses from the database.
     * @param course
     * @param days
     * @param start
     * @param end
     * @param id
     */
    public Course(String course, String days, int start, int end, int id){

        courseID = course;
        String[] dayArray = days.split(" ");
        for(int i = 0; i < dayArray.length; i++){
            Days.add(dayArray[i]);
        }
        startTime = start;
        endTime = end;
        ownerID = id;
    }

    /**
     * This is a constructor for internal use in identifying the schedule.
     * @param courseName This sets the full course ID of the object.
     * @param location	This is an internal value for location of the course data in the string.
     */
    public Course(String courseName, int location) {

        courseID = courseName;

        stringLocation = location;

    }

    /**
     * This is a method for pattern matching the time in a string, which is iterated over every course and its associated string.
     */
    public void findTime(){

        String time = "\\b" + "(?:[01]?[0-9])" + ":[0-5][0-9]" + " ?[ap]m" +
                " / " +
                "(?:[01]?[0-9])" + ":[0-5][0-9]" + " ?\r\n[ap]m" + "\\b";

        Pattern timePattern = Pattern.compile(time, Pattern.CASE_INSENSITIVE);
        Matcher matcher = timePattern.matcher(courseString);
        if(matcher.find()) {
            this.courseTime = courseString.substring(matcher.start(),matcher.end());
        }

        courseTime = courseTime.replaceAll("\\b" + " ?\r\n" + "\\b", " ");

        String arrayTime[] = courseTime.split(" / ", 2);
        Time.add(arrayTime[0]);
        Time.add(arrayTime[1]);
    }

    public void timeConvert(){
        startTime = Integer.parseInt(Time.get(0).replaceAll("[^0-9]", ""));
        if (Time.get(0).contains("PM") && !Time.get(0).contains("12")){
            startTime += 1200;
        }
        endTime = Integer.parseInt(Time.get(1).replaceAll("[^0-9]", ""));
        if (Time.get(1).contains("PM") && !Time.get(1).contains("12")){
            endTime += 1200;
        }

    }


    @Override
    public int compareTo(Course other) {
        return Integer.compare(this.stringLocation, other.stringLocation);
    }

}
