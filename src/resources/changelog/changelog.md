# 2022-12-05
   - Deep refactoring of LARVAFirstAgent DroidShips to cope with session alias automatically since all the future sessions will be shared amongst different stakeholders. A session alias is a single word with no white spaces in between.
      - All players taking part in a shared session must:
         - Agree on a password for the session, i.e. "CHOCOLATE"
         - include defSessionAlias("CHOCOLATE") in their setup and in their services in DF
         - Agree on the order of execution of the agens for a good operation
                  -  Now, when a session is open with an alias, the corresponding Session Manager appears in DF as "<alias>" and also as "<sessionKey>" so as it is very easy to identify these terms just by having an alias.
      - The agent who opens the session must appears as "OPEN <alias>". 
      - All the agents that take part in a shared session must also hava "TEAM <alias>"
      - The use of NPCs needs the use of an alias. In order to do that, LARVAFirstAgent provides a unique alias for every user wich cannot be shared to other teams. The safest way of defining an alias for a regular student-agent is during Open Problem as specified in the reference manual.
      - Any regular agent may open a shared session with an alias and its stepts would be the regular ones: checkin+openproblem+joinsession+closeproblem+checkout I will be the Host agent.
      - Any agent invited to a shared sesion would be slightly different. It will not have open/close problem because the Host agent already did it (or has the shortcut). It just takes the sessionAlias and explores the DF to get to know its associated Session manager, the session key, the Host agent and all its team mates.
   - Refactored the GUI. Now on top of older COMMAND box, it appears the sessionKey or the sessionAlias when there is one. On top of the payload appears the name of the agent who is using the XUI

# 2022-11-29
   - Updated new features in LARVADialogicalAgents
   - Added SingleBrosBrawl y SmahBrosBrawl
   - Fixed backdoor by LARVAFirstAgent.addMilestone(String m). Thanks to David Correa and his team. Exexcellent discovery that proves your deep understanding of LARVA, congrats!


# 2022-11-15
   - Serveral bug fixes
      - One that ITT were not allowed to CAPTURE nobody (damn!) because this capability of the agent was not activated in SensorDecoder (perhaps it never did)
      - Added TelegramBotsApi 6.1 as a new dependence of larva.core This means that, in order to compile this version, you must add this library to the project, that is, Projects-larva.core-Libraries-Add JAr folder. This new library  may be found in src/resource/dependencies
      
# 2022-10-25
   - Bugfixes
      - Remove the need to use performative and conversationID in the dialogue to DEST in Lab1. It is not required to do so.
   - Features
      - Telegram bot DBA Droid
         - All messages have a HTML format to highlight relevant information
         - Button "Notifications" renamed as "Preferences"
         - Button "Preferences" include the possibility of selecting the level of notifications one wants to receive: [Issue #7](https://github.com/Anatoli-Grishenko/es.ugr.larva.core/issues/7)
            - When a new milestone is achieved
            - When an already achieved milestone is re-achieved
            - When a problem is solved
            - Notifications related to the achievement of goals and missions
            - Notifications coming from Identity Managers (checkin, checkout)
            - Notifications coming from Problem Managers (open problem, close problem)
            - The reception of errors and warnings is mandatory and cannot be disabled

# 2022-10-01 Ready for Lab1 and Lab2
