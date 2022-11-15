
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
