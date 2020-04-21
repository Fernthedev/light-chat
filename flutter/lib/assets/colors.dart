import 'package:flutter/material.dart';

const kBluePrimary = Color(0xFF0285EE); // 100, 2, 133, 238
const kOrangeSecondary = const Color(0xFFEE6C02); // 100, 238, 108, 2
const kBrown = const Color(0xFF442B2D);
const kWhite = const Color(0xFFFFFBFA);
const kError = const Color(0xFFC5032B);

//Dark theme
const kGrey = Color(0x52FFFBFA);
//const kWater = const Color(0xFF9765F4);
//const kShrineGreen = const Color(0xFFC2F465);

ThemeData buildLightTheme() {
  final ThemeData base = ThemeData.light();
  return base.copyWith(
    primaryColor: kBluePrimary,
    primaryTextTheme: buildTextTheme(base.primaryTextTheme, kWhite),
    primaryIconTheme: base.iconTheme.copyWith(color: kWhite),
    buttonColor: kBluePrimary,
    accentColor: kBrown,
    scaffoldBackgroundColor: kWhite,
    cardColor: Colors.white,
    textSelectionColor: kBluePrimary,
    errorColor: kError,
    buttonTheme: ButtonThemeData(
      textTheme: ButtonTextTheme.accent,
    ),
    textSelectionHandleColor: kBluePrimary,
    accentTextTheme: buildTextTheme(base.accentTextTheme, kBrown),
    textTheme: buildTextTheme(base.textTheme, kBrown),
  );
}

TextTheme buildTextTheme(TextTheme base, Color color) {
  return base
      .copyWith(
        headline5: base.headline5.copyWith(
          fontWeight: FontWeight.w500,
        ),
        headline6: base.headline6.copyWith(fontSize: 18.0),
        caption: base.caption.copyWith(
          fontWeight: FontWeight.w400,
          fontSize: 14.0,
        ),
      )
      .apply(
        fontFamily: 'Rubik',
        displayColor: color,
        bodyColor: color,
      );
}

ThemeData buildDarkTheme() {
  final ThemeData base = ThemeData.dark();
  return base.copyWith(
    primaryColor: kGrey,
//    primaryTextTheme: _buildTextTheme(base.primaryTextTheme, kWhite),
//    primaryIconTheme: base.iconTheme.copyWith(color: kBluePrimary),
    buttonColor: kBluePrimary,
    accentColor: kBluePrimary,
//    cardColor: Colors.black,
    textSelectionColor: kWhite,
    errorColor: kError,
    buttonTheme: ButtonThemeData(
      textTheme: ButtonTextTheme.accent,
    ),
    textSelectionHandleColor: kBluePrimary,
//    accentTextTheme: _buildTextTheme(base.accentTextTheme, kWhite),
//    textTheme: _buildTextTheme(base.textTheme, kWhite),
  );
}
