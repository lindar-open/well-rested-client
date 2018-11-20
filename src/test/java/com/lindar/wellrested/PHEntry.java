package com.lindar.wellrested;

import lombok.Setter;

public class PHEntry {

    @Setter
    private int userId;
    @Setter
    private int id;
    @Setter
    private String title;
    @Setter
    private String body;

    public boolean equals(PHEntry other){
        return (this.userId==other.userId && this.body.equals(other.body) && this.id==other.id && this.title.equals(other.title));
    }

}
