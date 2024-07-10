if(NOT TARGET game-activity::game-activity)
add_library(game-activity::game-activity STATIC IMPORTED)
set_target_properties(game-activity::game-activity PROPERTIES
    IMPORTED_LOCATION "C:/Users/alexei/.gradle/caches/transforms-4/eb7d0302ac381986475583af9519a0e6/transformed/games-activity-1.2.2/prefab/modules/game-activity/libs/android.x86/libgame-activity.a"
    INTERFACE_INCLUDE_DIRECTORIES "C:/Users/alexei/.gradle/caches/transforms-4/eb7d0302ac381986475583af9519a0e6/transformed/games-activity-1.2.2/prefab/modules/game-activity/include"
    INTERFACE_LINK_LIBRARIES ""
)
endif()

if(NOT TARGET game-activity::game-activity_static)
add_library(game-activity::game-activity_static STATIC IMPORTED)
set_target_properties(game-activity::game-activity_static PROPERTIES
    IMPORTED_LOCATION "C:/Users/alexei/.gradle/caches/transforms-4/eb7d0302ac381986475583af9519a0e6/transformed/games-activity-1.2.2/prefab/modules/game-activity_static/libs/android.x86/libgame-activity_static.a"
    INTERFACE_INCLUDE_DIRECTORIES "C:/Users/alexei/.gradle/caches/transforms-4/eb7d0302ac381986475583af9519a0e6/transformed/games-activity-1.2.2/prefab/modules/game-activity_static/include"
    INTERFACE_LINK_LIBRARIES ""
)
endif()

