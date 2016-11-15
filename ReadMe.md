# The Alpha Fitness Android App .

Track your steps, workouts, and see the live updates on the Map.

__Project is created using__

> Google Material Design Guidelines    
> Multi- Threaed MVC architecture  
> Location and Step Sensors  
> Google Map  
> Remote Service  
> Async Tasks  
> Content Providers  
> Clouds - Firebase (To Store user information)  
> Sqlite Db - Using Content Providers

**If you have any questions or want to contribute to my project feel free to email me at
swapnilpatil427@gmail.com**  



![Screenshot](https://github.com/swapnilpatil427/AlphaFitness/blob/master/screenshots/Screenshot.png) 
![Screenshot](https://en.wikipedia.org/wiki/File:Path_example.JPG)  
![Screenshot](https://en.wikipedia.org/wiki/File:Path_example.JPG)
![Screenshot](https://en.wikipedia.org/wiki/File:Path_example.JPG)

# A) Designing User Interface
The Alpha Fitness app (Figure 1) has several Activities. The first Activity that will be
launched at the start time is RecordWorkout Activity. 

It has a fragment for portrait mode and a fragment for landscape mode. When RecordWorkout is in the portrait mode, the user can press the Start Workout button. Once clicked, the Start Workout button becomes the Stop Workout button. The distance and duration of the workout will be recorded in real time and shown on the screen. 

The route for the user will be recorded using Android’s GPS
services and shown on Google Map. When the user rotates the screen, the Record Workout
Activity goes into the landscape more. The Workout Details fragment will be shown.

In the Record Workout Activity, there is the User Profile button. When the user clicks on
this button, the second Activity screen for user profile will be activated. User can edit name,
select gender and input weight information. The same screen also gives summaries such as
weekly average and all time total for distance, time, number of workouts and calories burned.
User can use the Android’s Go Back button to navigate back to the Record Workout screen.


# B) Recording for Workout Session
The user can turn off the screen of Android phone or launch other applications after a
workout session has started (after the user clicked on the Start Workout button). In order to
keep recording the workout data, i hvae used a Remote Service that is in a different
process from that of the activities. At the appropriate time, each workout session data will be
saved to SQLite database on the phone using Content Provider.

# C) Using GPS and Google Map
Besides the step counts, you’ll take advantage of the build-in GPS feature on Android phone
to show the path of user’s workout session. Android provides a set of Google Map APIs for
Android as well as very good information on how to integrate Google Map and navigation
into your app. You’ll find more information here:
https://developers.google.com/maps/documentation/android-api/start.

# D) Estimating Calories and Distance from Step Counts
Both Calories and distance can be estimated from the step counts. You can make reasonable
assumption for both calories burnt and the stride length for each step.
This article shows you
a chart that converts calories burned walking from step count and the user’s weight:

https://www.verywell.com/pedometer-steps-to-calories-converter-3882595.     

You can also find the distance from steps:  

https://www.verywell.com/set-pedometer-better-accuracy-3432895   

For plotting real-time data for Calories and steps per 5 minutes for the Workout Details
screen, I have used MPAndroidChart:  
https://github.com/PhilJay/MPAndroidChart    
in my program. The link takes you to the official web site for MPAndroidChart on Github.com.
You will find documentation as well as good examples on the website.





