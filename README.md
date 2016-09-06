


This is the backend of a simple application like IMDB.


To Compile: execute `mvn clean install`
To Run: execute `sh run.sh`



This is a very basic implementation of the backend of an IMDB like website. The following features are supported.

1. Movies Add/Edit/Search
  a. All the data is in persisted in Mysql. We would also store Movie names,id and cast/crew names in Elastic search against Movie ID to implement Smart Search when the user is searching something from the UI. From ES we’ll fetch the Movie ID and then show the corresponding details by Querying MySQL for the search piece. Not putting it in ES right now, but it’ll be a trivial change to do that. I’ll update the code to do this after some time.

2. Cast/Crew Add/Edit/Search
  a. All the data is in Mysql. Name is in ES to support search
3. User Provisioning for customers.
  a.All data in Mysql in Users table
4. API for users to Rate and Review
  a. Rating goes into Ratings table, Reviews go into reviews table
  b. In writes for ratings, also write into a table that has rating count, rating sum against a movie. Preferring this against a N minute cache on Redis,etc because reads are writes are frequent for some movies and both fall for older movies, so for older movies cache would always timeout.
5. Recommendation
  a. Recommendation is built based on the following logic: 
    i. If the users has done some ratings: get the shows that the user has rated 4/5 stars, then get their genres, then find the other shows in that genres ordered by their rating. return top 10.
    ii. If the user has not rated anything, return the top 10 rated shows .
  b. Ideally this is not the best way, and in the real world we could have used some existing recommendation engine say Neo4J to build that. I am not using that for now because I have not worked on that in the past, so it would take some time to read about that.
6. Get Aggregated ratings for a movie. Gets from a table storing aggregated rating.
7. Get all the reviews of a movie with pagination, Upvote/Downvote reviews and comment on reviews 

