package me.sha425.ourpleasure.dto;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "hentai")
@Data
@NoArgsConstructor
public class HentaiEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    private String blot;

    @Column(nullable = false)
    private String url;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    private String thumbnail;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String year;

    @Column(nullable = false)
    private String series;

    @Column(nullable = false)
    private boolean censorship;

    @Column(name = "average_score", nullable = false)
    private double averageScore;

    @Column(nullable = false)
    private String genres;

    @Column(name = "voice_over", nullable = false)
    private String voiceOver;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private String story;

    public void setThumbnail(String thumbnail) {
        if ("обновлено".equals(thumbnail)) {
            this.thumbnail = "https://cdn-icons-png.flaticon.com/512/5619/5619908.png";
        } else if ("новое".equals(thumbnail)) {
            this.thumbnail = "https://i.ibb.co/dDfb8L0/free-icon-new-4817673.png";
        } else {
            this.thumbnail = thumbnail;
        }
    }
}
