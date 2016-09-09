package com.example.myapplication.holder;


public class PeriodHolder
{

    public String start;
    public String end;
    public String name;

    public PeriodHolder(String name, String start, String end)
    {
        this.start = start;
        this.end = end;
        this.name = name;
    }


    @Override
    public String toString()
    {
        return start + " -> " + end;
    }
}
