# Contributing to the Realm Documentation for Engineers

Hello! If you're reading this, you're probably a software developer working on MongoDB Realm.
This is a guide to help you guest write content for the [realm docs](https://docs.mongodb.com/realm/).
Here's a quick overview of how this system works:

## Overview

1) You get an idea for content that you'd like to see in the docs. Example: "Users would benefit from a
   guide that describes how to use Realm Functions to call the Twilio SDK instead of the
   soon-to-be-deprecated Twilio Service"
  
2) Put together a very brief 2-3 sentence description of the goals and extent of your idea. Example:
   "The 'Using the Twilio SDK in Realm Functions' guide would A) inform users of how to make the
   Twilio SDK available in Functions using NPM and External Dependencies, B) describe how to
   access that dependency in a Function, and C) demonstrate how to use the Twilio SDK in a function
   to send a text message. The Guide would NOT describe how to install NPM from scratch, how to use
   the Twilio SDK for any tasks more complicated than sending a text message, or how to set up a
   Twilio developer account to send text messages on their free tier (though the Guide would link
   to content for users who need that guidance).

3) Post that brief description in the docs-realm Slack channel and tag the Realm docs team with
   `@developer-education-team`

4) You and the Realm docs team will discuss how to best fit this content into the documentation, including:
   
   - The usefulness of such a page to our users (what percentage of users would benefit from
     this page? Does it satisfy a common request from the community?)
     
   - How often the docs team will need to update the page as APIs and dependencies change
     ("this page describing how to best build a hosted React site requires maintenance every
      3 days when best practices change" "this page describing how to write a Realm Function
      that prints out ASCII art of a cactus would only require maintenance if we seriously
      change how Realm Functions work")

   - Whether or not the pitched page's goal is already satisfied by existing or planned docs
     content

   - Where the page belongs in the Realm documentation organization hierarchy

   - A rough outline of how this page might look (headers, which images and code snippets
     to include, rough ideas of what descriptive text we'll need)

5) Once we figure out the answers to those questions, it's time to write. Take the results
   of the previous step's conversation and write your rough draft in a
   Google Doc. The Realm docs team will handle getting the
   final draft into the RST format we use for our documentation once it's approved.

6) Send a link to your rough draft to `@developer-education-team` on Slack.

6) Review and editing! The team will take a look at your rough draft and
   ask questions, suggest minor changes, and generally brainstorm how to make your ideas
   look best on the docs platform. A lot of these will end up being things like "can we
   clarify this statement" and "we should use active voice here instead of passive voice"
   and other small changes to better follow the
   [MongoDB Documentation Style Guide](https://docs.mongodb.com/meta/style-guide/). Other
   changes could involve things like making code snippets more readable and moving around
   the ordering of sections to better match the look and feel of other Realm Docs pages.

7) LGTM! When the team says that your draft looks good, they'll take the content
   you've created and port it over to RST for docs content in a PR on
   [docs-realm](https://github.com/mongodb/docs-realm). We'll give it a last docs-internal
   copy edit, and add you as a reviewer.

8) When you've given the final approval, we'll merge your page in and roll it out to the
   production Realm Docs site. The docs team will shower you with bonusly and compliments
   for your efforts and overwhelmingly recommend you for a promotion.

Sound good? Think about the content that *you* know really well that the Realm Docs site
lacks! Then, reach out -- we're happy to help you every step of the way.
