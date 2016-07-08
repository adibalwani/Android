package edu.neu.madcourse.adibalwani.finalproject.leaderboard;

/**
 * Created by rachit on 26-03-2016.
 */
public class User implements Comparable<User> {

    private String name;
    private int score;

    public User() {

    }

    public User(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public int compareTo(User other) {
        int p1Score = this.getScore();
        int p2Score = other.getScore();
        if (p1Score > p2Score) {
            return -1;
        } else if (p1Score < p2Score) {
            return 1;
        } else {
            return 0;
        }
    }
}
