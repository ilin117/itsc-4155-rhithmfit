package com.example.rhithmfit;

import com.example.rhithmfit.classes.Song;
import org.junit.Test;
import static org.junit.Assert.*;

public class SongTest {

    @Test
    public void testSongConstructorAndGetters() {
        Song song = new Song("Shape of You", "Ed Sheeran", "12345");

        assertEquals("Shape of You", song.getTitle());
        assertEquals("Ed Sheeran", song.getArtist());
        assertEquals("12345", song.getId());
    }

    @Test
    public void testSongToString() {
        Song song = new Song("Shape of You", "Ed Sheeran", "12345");

        assertEquals("Shape of You – Ed Sheeran", song.toString());
    }

    @Test
    public void testSongDefaultConstructor() {
        Song song = new Song();
        assertNull(song.getTitle());
        assertNull(song.getArtist());
        assertNull(song.getId());
    }
}
