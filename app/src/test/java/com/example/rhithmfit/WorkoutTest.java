package com.example.rhithmfit;

import com.example.rhithmfit.classes.Workout;
import org.junit.Test;
import static org.junit.Assert.*;

public class WorkoutTest {

    @Test
    public void testWorkoutConstructorWithNameAndId() {
        Workout workout = new Workout("Leg Day", "abc123");

        assertEquals("Leg Day", workout.getDisplayName());
        assertEquals("abc123", workout.getId());
    }

    @Test
    public void testWorkoutConstructorWithIntensityDateId() {
        Workout workout = new Workout("High", "2025-10-27", "xyz789");

        assertEquals("High", workout.getIntensity());
        assertEquals("2025-10-27", workout.getDate());
        assertEquals("xyz789", workout.getId());
        assertNull(workout.getDisplayName()); // name not set in this constructor
    }

    @Test
    public void testWorkoutDefaultConstructor() {
        Workout workout = new Workout();

        assertNull(workout.getIntensity());
        assertNull(workout.getDate());
        assertNull(workout.getId());
        assertNull(workout.getDisplayName());
    }
}
