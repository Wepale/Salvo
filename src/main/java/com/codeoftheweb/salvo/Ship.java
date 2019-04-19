package com.codeoftheweb.salvo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.util.*;

@Entity
public class Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private String type;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayer_id")
    private GamePlayer gamePlayer;

    @ElementCollection
    private List<String> location = new ArrayList<>();

    public Ship() { }

    public Ship(String type, List<String> location) {
        this.type = type;
        this.location = location;

    }


    public long getId() {
        return this.id;
    }

    @JsonIgnore
    public GamePlayer getGamePlayer() {
        return this.gamePlayer;
    }

    public List<String> getLocation() {
        return this.location;
    }

    public String getType() {
        return this.type;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public Map<String, Object> toDTO() {
        return new LinkedHashMap<String, Object>(){{
            put("type", getType());
            put("locations", getLocation());
        }};
    }
}
