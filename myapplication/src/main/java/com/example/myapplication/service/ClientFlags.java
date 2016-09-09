package com.example.myapplication.service;

public interface ClientFlags
{

    int FLAG_LESSON_SCORE = 1 << 0;//1
    int FLAG_PUNISHMENT = 1 << 1;//2
    int FLAG_ABSENT = 1 << 2;//4
    int FLAG_ENCOURAGEMENT = 1 << 3;//8
    int FLAG_CURRICULUM = 1 << 4;//16
    int FLAG_CHAT = 1 << 5;//32
    int FLAG_STUDENT = 1 << 6;//64


}
