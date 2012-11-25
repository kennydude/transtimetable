# TransTimetable

Android Transit Application.

But, that's not all! You can easily add your own plugins to provide as many different transit services as your heart contents

## Plugin API

Make a new Android project, but don't create any activities then add a script (linux users) with the following:

	wget http://kennydude.github.com/transtimetable/translib.jar -o libs/translib.jar

This makes sure you have the latest version of the library, when I release a new version, just re-run the script! Easy! (but don't worry, we use JSON so the app won't break between versions) :D

Next you need to declare a service:

       <service
            android:exported="true"
            android:label="@string/national_rail"
            android:permission="me.kennydude.transtimetable.USE_TRANSIT_DATA"
            android:name="me.kennydude.transtimetable.data.NationalRail">
            
            <intent-filter>
                <action android:name="me.kennydude.TRANSIT_INFORMATION" />
            </intent-filter>
            
        </service>

(although change the name and label)

Now you need to make that class and make it extend `TransService`.

Next implement all of it's abstract functions

Finally, have a cuppa tea while you run it on your device :)
