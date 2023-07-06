# Cubic-Spline-Interpolator

A Java implementation of a cubic spline interpolation and mapping program for smooth path generation in FTC robotics. Graphing powered by JFree charts.

**V2 Updates:** Previously splines were contrained to being ordered in the increasing x direction, now an adaptive spline algorithm has been implemented, allowing for curves to loop back in over themselves. This allows users to easily plug in any number of points while having to run the program once. 
Moreover, program crash when plugging in consecutive points with the same x coordinate as been prevented through linear spline safeties. 
# Instructions

Input: Enter the N number of points followed by the points themselves. 

Output: A series of piecewise,cubic equations connecting all N points along with a graph displaying the spline path.

Sample Input:

```
8
9 16
22 22
41 19
53 8
60 -51
38 -53
15 -45
0 0
```

Sample Output:

![alt text](https://github.com/MrinallU/FTC-Cubic-Spline-Interpolation/blob/main/example.png?raw=true)
