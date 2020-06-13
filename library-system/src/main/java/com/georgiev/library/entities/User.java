package com.georgiev.library.entities;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    private int id;
    private String username;
    private String profilePictureUrl;
    private String accessToken;
    private String accessTokenSecret;
    private Set < Quote > quotes = new HashSet < > ();
    private Set < BookList > bookLists = new HashSet < > ();
    private boolean shareActivity;
    private Set < Book > startedBooks = new HashSet < > ();
    private Set < Book > finishedBooks = new HashSet < > ();
    private Set < Book > downloadedBooks = new HashSet < > ();

    public User(String username, String profilePictureUrl, String accessToken, String accessTokenSecret, Set < Quote > quotes, Set < BookList > bookLists, boolean shareActivity, Set < Book > startedBooks, Set < Book > finishedBooks, Set < Book > downloadedBooks) {
        this.username = username;
        this.profilePictureUrl = profilePictureUrl;
        this.accessToken = accessToken;
        this.accessTokenSecret = accessTokenSecret;
        this.quotes = quotes;
        this.bookLists = bookLists;
        this.shareActivity = shareActivity;
        this.startedBooks = startedBooks;
        this.finishedBooks = finishedBooks;
        this.downloadedBooks = downloadedBooks;
    }

    public User() {}
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    public Set < Quote > getQuotes() {
        return quotes;
    }

    public void setQuotes(Set < Quote > quotes) {
        this.quotes = quotes;
    }

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    public Set < BookList > getBookLists() {
        return bookLists;
    }

    public void setBookLists(Set < BookList > bookLists) {
        this.bookLists = bookLists;
    }

    public boolean getShareActivity() {
        return shareActivity;
    }

    public void setShareActivity(boolean shareActivity) {
        this.shareActivity = shareActivity;
    }

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "user_startedBooks",
            joinColumns = {
                    @JoinColumn(name = "user_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "book_id")
            })
    public Set < Book > getStartedBooks() {
        return startedBooks;
    }

    public void setStartedBooks(Set < Book > startedBooks) {
        this.startedBooks = startedBooks;
    }
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "user_finishedBooks",
            joinColumns = {
                    @JoinColumn(name = "user_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "book_id")
            })
    public Set < Book > getFinishedBooks() {
        return finishedBooks;
    }

    public void setFinishedBooks(Set < Book > finishedBooks) {
        this.finishedBooks = finishedBooks;
    }

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "user_downloadedBooks",
            joinColumns = {
                    @JoinColumn(name = "user_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "book_id")
            })
    public Set < Book > getDownloadedBooks() {
        return downloadedBooks;
    }

    public void setDownloadedBooks(Set < Book > downloadedBooks) {
        this.downloadedBooks = downloadedBooks;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }

    public void setAccessTokenSecret(String accessTokenSecret) {
        this.accessTokenSecret = accessTokenSecret;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || obj.getClass() != this.getClass())
            return false;

        User user = (User) obj;
        return (user.id == this.id && user.getUsername().equals(this.username) && new HashSet < > (user.getBookLists()).equals(new HashSet < > (this.bookLists)) && new HashSet < > (user.getQuotes()).equals(new HashSet < > (this.quotes)));
    }

    @Override
    public int hashCode() {
        return this.id * username.length();
    }
}