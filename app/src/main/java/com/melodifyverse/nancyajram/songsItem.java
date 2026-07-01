package com.melodifyverse.nancyajram;

public class songsItem {

    int favourite;
    String Title;
    int userPhoto;
    int songID ;
    private int colorIndex = -1;
    public songsItem(int favourite,String title,int songID ,int userPhoto) {
        this.favourite = favourite;
        Title = title;
        this.userPhoto = userPhoto;
        this.songID = songID;
    }

    public void setFavourite(int favourite) { this.favourite = favourite;}
    public int getFavourite() {
        return favourite;
    }
    public String getTitle() {
        return Title;
    }
    public int getUserPhoto() {
        return userPhoto;
    }

    public int getColorIndex() {
        return colorIndex;
    }
    public void setColorIndex(int colorIndex) {
        this.colorIndex = colorIndex;
    }
}
