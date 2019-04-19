package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
public class Salvo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private Integer turn;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    private GamePlayer gamePlayer;

    @ElementCollection
    private List<String> locations;

    public Salvo(){}

    public Salvo(Integer turn, List<String> locations){
        this.turn =turn;
        this.locations = locations;
    }

    public List<String> getLocations() {
        return this.locations;
    }

    public long getId() {
        return this.id;
    }

    @JsonIgnore
    public GamePlayer getGamePlayer() {
        return this.gamePlayer;
    }

    public Integer getTurn() {
        return this.turn;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public Map<String, Object> toDTO() {
        return new LinkedHashMap<String, Object>(){{
            put("playerId", gamePlayer.getPlayer().getId());
            put("turn", turn);
            put("locations", locations);
        }};
    }
}
