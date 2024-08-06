package com.rubio.converter.backend.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Inspiration {
    private static List<String> inspirations = new ArrayList<String>() {{
        add("Let's process 4k videos from mobile devices without data loss. WCGW ?");
        add("Real men process 4k videos without connecting the charger.");
        add("Crossing your fingers helps to convert videos successfully.");
        add("If there is memory ... there is hope. ");
        add("May the processing power be with you!");
        add("4K? No problem! (Just don't expect me to move for the next hour)");
        add("I'm not lagging, I'm just rendering... my sanity");
        add("My phone's heating up, but my edits are fire");
        add("Quality loss? What's that? (Just kidding, I'm waiting for it to render)");
        add("I'm not staring at a loading bar, I'm watching a tiny progress movie");
        add("4K video editing: because I hate my phone's battery life");
        add("Rendering... and contemplating life choices");
        add("My device is cooking, but the video will be worth it (hopefully)");
        add("Diving into video editing, where the only thing more abundant than seaweed is lag");
        add("Underwater videos: where the pressure is high, but my phone's processor is higher");
        add("I'm not editing, I'm just trying to surface from this rendering ocean");
        add("My phone's heating up, but the fish in my videos are still chill");
        add("4K underwater video editing: because I want to see every grain of sand in excruciating detail");
        add("I've seen coral reefs grow faster than this video export");
        add("Underwater video editing: where the only thing more slow-moving than the fish is my progress");
        add("I'm starting to think that 'quality loss' is just a myth perpetuated by scuba divers with slow computers");
        add("My device is so hot, I think it's trying to cook the virtual fish");
        add("Underwater video editing: the ultimate test of willpower (and battery life), now with added ocean pressure");
        add("I've seen sea turtles move faster than this rendering progress, and they're carrying shells!");
        add("I'm not rendering, I'm just trying to find Nemo... in my timeline");
        add("Underwater video editing: where the pressure is high, but my patience is lower");
        add("My phone's processor is working harder than a shrimp at a seafood buffet");
        add("4K underwater video editing: because I want to see every fishy detail... eventually");
        add("I've seen ocean currents move faster than this video export");
        add("Underwater video editing: where the only thing more slow-moving than the fish is my progress... and my internet connection");
        add("I'm starting to think that 'quality loss' is just a myth perpetuated by dolphins with slow computers");
        add("My device is so hot, I think it's trying to boil the virtual seawater");
        add("Underwater video editing: the ultimate test of willpower (and battery life), now with added ocean pressure... and a side of seaweed snacks");
        add("I've seen squid change color faster than this rendering progress... and they don't even have a GPU!");
    }};

    public static String getRandomPhrase() {
        Collections.shuffle(inspirations);
        return inspirations.get(0);
    }
}
