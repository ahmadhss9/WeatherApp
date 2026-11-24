public static int iconFor(int code, boolean isDay) {

    switch (code) {

        case 0:                                         // Step 36: clear sky
            return isDay ? R.drawable.day : R.drawable.night;

        case 1:                                         // Step 37: mainly clear
            return isDay ? R.drawable.cloudy_day_1 : R.drawable.cloudy_night_1;

        case 2:                                         // Step 37: partly cloudy
            return isDay ? R.drawable.cloudy_day_2 : R.drawable.cloudy_night_2;

        case 3:                                         // Step 37: overcast
            return isDay ? R.drawable.cloudy_day_3 : R.drawable.cloudy_night_3;

        case 45:                                        // Step 37: fog
        case 48:
            return R.drawable.cloudy;

        default:                                        // rain/snow/storm: steps 38-39
            return R.drawable.cloudy;
    }
}