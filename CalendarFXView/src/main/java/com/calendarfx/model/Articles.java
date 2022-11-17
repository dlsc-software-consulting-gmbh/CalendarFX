package com.calendarfx.model;

import java.util.ArrayList;
import java.util.List;

public class Articles {

    private List<String> articleList1;
    private List<String> articleList2;
    private List<String> articleList3;
    private List<String> articleList4;

    public Articles(){
        System.out.println("ARTICLE");
        articleList1 = new ArrayList<>();
        articleList2 = new ArrayList<>();
        articleList3 = new ArrayList<>();
        articleList4 = new ArrayList<>();

        articleList1.add("Keeping Up With a Changing Workload");
        articleList1.add("Guide to a Better Work/Life Balance");
        articleList1.add("Mindfulness: a New Approach");
        articleList2.add("Uplifting Stories This Week");
        articleList2.add("Holding Your Own When Facing New Challenges");
        articleList2.add("Pressed for time? Don't put all your energy in one place.");
        articleList3.add("Breathing techniques that actually work");
        articleList3.add("\"Keeping it together\", on your own terms");
        articleList3.add("The Perspectives of the Everyday Occurance");
        articleList4.add("Reaching Your Baseline");
        articleList4.add("Resources: Know who to talk to");
        articleList4.add("You're never alone, what others have to say");
        articleList4.add("The next day: What tomorrow really means");

    }

    public List<String> getList1(){
        return articleList1;
    }
    public List<String> getList2(){
        return articleList2;
    }
    public List<String> getList3(){
        return articleList3;
    }
    public List<String> getList4(){
        return articleList4;
    }
}
