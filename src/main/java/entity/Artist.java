package entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Artist {

    private String name;
    private String image;
    private List<Integer> playcounts;

    public Artist(String name, String image, int playcount) {
        this.name = name;
        this.image = image;
        playcounts = new ArrayList<>();
        playcounts.add(playcount);
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public List<Integer> getPlaycounts() {
        return playcounts;
    }

    public void addPlaycount(int playcount) {
        playcounts.add(playcount);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Artist other = (Artist) obj;
        return this.name.equals(other.name);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.name);
        return hash;
    }
}
