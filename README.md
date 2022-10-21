# APPERÓ

Apperó is an app that helps its users annotate tasks they have to do,
with a particular focus on letting them do it as fast as possible,
providing the possibility to attach various types of content ()
and synchronizing with other applications.


#Features:
- Recording audio attachments
- Setting up notifications
- Setting up calendar events
- Soon to be: Adding tasks to Notion databases

#Navigation:
![alt text](app/Navigation-Navigation_Diagram.png)
<br></br>
The app is composed of 2 main composables, todo_list and add_edit_todo,
inflated from MainActivity.kt, using Jetpack Compose way of using the navigation component,
meaning without a navigation graph. A NavController is created inside the theme,
from there the NavHost inflates the composables and passes them 
the NavController method they need to navigate, as shown in the image above.
<br></br>
I separated add_todo and edit_todo, although they are actually the same composable,
because they are reached using different NavController.navigate strings. Except for this, from both
going back to todo_list is done using the NavController.popBackStack method.

#MVVM:





