# HawaMahal
Sparkathon App
## Inspiration
* Weather and time of the day play very important role in deciding which form of the music a user would listen to. Indian classical music has different "ragas" for different times of the day. 

* There are some "ragas" which are related to "weather" conditions as well. As an example, according to legend, raga Malhar is so powerful that when sung, it can induce rainfall. ( Referred from Wikipedia).
In general human emotions very according to the weather and hence the song selection too. 

* "Hawa" in Hindi means "Wind" and "Mahal" stands for palace. Since this app uses "weather conditions" as a primary function for selecting songs, named it HawaMahal. There also used to be a Bollywood classic songs program named "HawaMahal" on "All India Radio" in past (Not sure if it is still going on) 


## What it does
* HawaMahal uses Spark and Scala to process historical weather data and songs played by users in the vicinity of user's location to come up with the song recommendations.

## How I built it
* Retrieved the historical weather data for year 2015 and moved it to Swift Object storage.
* Got hold of few random bollywood songs from Spotify and used their developer APIs to fetch the information that I was interested in.
* Generated and uploaded the dataset that mapped those songs to each day of the year 2015. This is absolutely random but proves the idea.
* By joining these two datasets figured out the relationship between weather conditions ( Temperature for now) and songs played.
* Android app that figures out user's location and weather condition in real-time, uses this data to talk to Swift Object storage to retrieve the recommendations.

## Challenges I ran into
* I was hoping to get anonymous data about daywise list of Songs played in particular region. As I did not find anything like it, I had to cook my own dataset.
* Scala Notebooks that I had created at the start of hackathon was not working and it took a little time and IBM's assistance to figure out the issues.

## Accomplishments that I'm proud of
* Completion of another project from conceptualization to implementation.
* Felt creative while preparing the Android App logo for this app. I have also animated the musical notes in logo which gets displayed while app fetches the song details.

## What I learned
* Substantial Spark and Scala learning opportunity 
* Android APIs 
* Spotify Developer APIs

## What's next for HawaMahal
* Spotify app sends out a broadcast event that other apps can listen to. This event notifies which song is being played in Spotify player. I could use that to build the anonymous music database that has date, time and location of the song played.
