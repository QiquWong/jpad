package sandbox2.vt.charts;

import eu.hansolo.fx.charts.Axis;
import eu.hansolo.fx.charts.ChartType;
import eu.hansolo.fx.charts.Position;
import eu.hansolo.fx.charts.XYChart;
import eu.hansolo.fx.charts.XYPane;
import eu.hansolo.fx.charts.XYZPane;
import eu.hansolo.fx.charts.YPane;
import eu.hansolo.fx.charts.data.XYData;
import eu.hansolo.fx.charts.data.XYDataObject;
import eu.hansolo.fx.charts.data.XYZData;
import eu.hansolo.fx.charts.data.XYZDataObject;
import eu.hansolo.fx.charts.data.YData;
import eu.hansolo.fx.charts.data.YDataObject;
import eu.hansolo.fx.charts.series.XYZSeries;
import eu.hansolo.fx.charts.series.YSeries;
import eu.hansolo.fx.charts.series.XYSeries;
import eu.hansolo.fx.charts.unit.Unit;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by hansolo on 16.07.17.
 */
public class TestChartsHanSolo extends Application {
    private enum Alignment { TOP, RIGHT, BOTTOM, LEFT, CENTER }
    private static final Double          AXIS_WIDTH     = 25d;
    private static final Color[]         COLORS         = { Color.RED, Color.BLUE, Color.CYAN, Color.LIME };
    private static final Random          RND            = new Random();
    private static final int             NO_OF_X_VALUES = 100;
    private XYSeries<XYDataObject>       xySeries1;
    private XYSeries<XYDataObject>       xySeries2;
    private XYSeries<XYDataObject>       xySeries3;
    private XYSeries<XYDataObject>       xySeries4;

    private XYChart<XYDataObject>        lineChart;
    private Axis                         lineChartXAxisBottom;
    private Axis                         lineChartYAxisLeft;
    private Axis                         lineChartYAxisRight;

    private XYChart<XYDataObject>        areaChart;
    private Axis                         areaChartXAxisBottom;
    private Axis                         areaChartYAxisLeft;

    private XYChart<XYDataObject>        smoothLineChart;
    private Axis                         smoothLineChartXAxisBottom;
    private Axis                         smoothLineChartYAxisLeft;

    private XYChart<XYDataObject>        smoothAreaChart;
    private Axis                         smoothAreaChartXAxisBottom;
    private Axis                         smoothAreaChartYAxisLeft;

    private XYChart<XYDataObject>        scatterChart;
    private Axis                         scatterChartXAxisBottom;
    private Axis                         scatterChartYAxisLeft;

    private YSeries<YDataObject>         ySeries;
    private YPane<YDataObject>           donutChart;

    private XYZSeries<XYZDataObject>     xyzSeries;
    private XYZPane<XYZDataObject>       bubbleChart;

    private Thread                       modificationThread;

    private long                         lastTimerCall;
    private AnimationTimer               timer;


    @Override public void init() {
        List<XYDataObject>  xyData1 = new ArrayList<>(20);
        List<XYDataObject>  xyData2 = new ArrayList<>(20);
        List<XYDataObject>  xyData3 = new ArrayList<>(20);
        List<YDataObject>   yData   = new ArrayList<>(20);
        List<XYZDataObject> xyzData = new ArrayList<>(20);
        for (int i = 0 ; i < NO_OF_X_VALUES ; i++) {
            xyData1.add(new XYDataObject(i, RND.nextDouble() * 15, "P" + i, COLORS[RND.nextInt(3)]));
            xyData2.add(new XYDataObject(i, RND.nextDouble() * 15, "P" + i, COLORS[RND.nextInt(3)]));
            xyData3.add(new XYDataObject(i, RND.nextDouble() * 15, "P" + i, COLORS[RND.nextInt(3)]));
        }
        for (int i = 0 ; i < 20 ; i++) {
            yData.add(new YDataObject(RND.nextDouble() * 10, "P" + i, COLORS[RND.nextInt(3)]));
            xyzData.add(new XYZDataObject(RND.nextDouble() * 10, RND.nextDouble() * 10, RND.nextDouble() * 25,"P" + i, COLORS[RND.nextInt(3)]));
        }

        xySeries1 = new XYSeries<>(xyData1, ChartType.LINE);
        xySeries2 = new XYSeries<>(xyData2, ChartType.AREA);
        xySeries3 = new XYSeries<>(xyData3, ChartType.SMOOTH_LINE);
        xySeries4 = new XYSeries<>(xyData1, ChartType.SMOOTH_AREA);

        //xySeries1.setShowPoints(false);
        //xySeries2.setShowPoints(false);
        //xySeries3.setShowPoints(false);
        //xySeries4.setShowPoints(false);


        ySeries    = new YSeries(yData, ChartType.DONUT);
        donutChart = new YPane(ySeries);

        xyzSeries   = new XYZSeries(xyzData, ChartType.BUBBLE);
        bubbleChart = new XYZPane(xyzSeries);

        // LineChart
        Unit   tempUnit          = new Unit(Unit.Type.TEMPERATURE, Unit.Definition.CELSIUS); // Type Temperature with BaseUnit Celsius
        double tempFahrenheitMin = tempUnit.convert(0, Unit.Definition.FAHRENHEIT);
        double tempFahrenheitMax = tempUnit.convert(20, Unit.Definition.FAHRENHEIT);

        lineChartXAxisBottom = createBottomXAxis(0, NO_OF_X_VALUES, true);
        lineChartYAxisLeft   = createLeftYAxis(0, 20, true);
        lineChartYAxisRight  = createRightYAxis(tempFahrenheitMin, tempFahrenheitMax, false);
        lineChart = new XYChart<>(new XYPane(xySeries2, xySeries1),
                                  lineChartYAxisLeft, lineChartYAxisRight, lineChartXAxisBottom);


        // AreaChart
        areaChartXAxisBottom = createBottomXAxis(0, NO_OF_X_VALUES, true);
        areaChartYAxisLeft   = createLeftYAxis(0, 20, true);
        areaChart            = new XYChart<>(new XYPane(xySeries2),
                                             areaChartXAxisBottom, areaChartYAxisLeft);

        xySeries2.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop(0.0, Color.rgb(255, 0, 0, 0.6)), new Stop(1.0, Color.TRANSPARENT)));
        xySeries2.setStroke(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop(0.0, Color.rgb(255, 0, 0, 1.0)), new Stop(1.0, Color.TRANSPARENT)));
        areaChart.getXYPane().setChartBackgroundPaint(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop(0.0, Color.rgb(50, 50, 50, 0.25)), new Stop(1.0, Color.rgb(25, 25, 25, 0.8))));

        // SmoothLineChart
        smoothLineChartXAxisBottom = createBottomXAxis(0, NO_OF_X_VALUES, true);
        smoothLineChartYAxisLeft   = createLeftYAxis(0, 20, true);
        smoothLineChart            = new XYChart<>(new XYPane(xySeries3),
                                                   smoothLineChartYAxisLeft, smoothLineChartXAxisBottom);

        // SmoothAreaChart
        smoothAreaChartXAxisBottom = createBottomXAxis(0, NO_OF_X_VALUES, true);
        smoothAreaChartYAxisLeft   = createLeftYAxis(0, 20, true);
        smoothAreaChart            = new XYChart<>(new XYPane(xySeries4),
                                                   smoothAreaChartYAxisLeft, smoothAreaChartXAxisBottom);

        xySeries4.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop(0.0, Color.rgb(255, 255, 255, 0.6)), new Stop(1.0, Color.TRANSPARENT)));
        xySeries4.setStroke(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop(0.0, Color.rgb(255, 255, 255, 1.0)), new Stop(1.0, Color.TRANSPARENT)));
        smoothAreaChart.getXYPane().setChartBackgroundPaint(Color.rgb(25, 25, 25, 0.8));


        // ScatterChart
        scatterChartXAxisBottom = createBottomXAxis(0, NO_OF_X_VALUES, true);
        scatterChartYAxisLeft   = createLeftYAxis(0, 20, true);
        scatterChart            = new XYChart<>(new XYPane(xySeries1),
                                                scatterChartXAxisBottom, scatterChartYAxisLeft);

        modificationThread = new Thread(() -> {
            while(true) {
                List<XYData> xyItems = xySeries3.getItems();
                xyItems.forEach(item -> item.setY(RND.nextDouble() * 15));
                xySeries3.refresh();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(final long now) {
                //if (now > lastTimerCall + 1_000_000_000l) {
                if (now > lastTimerCall) {
                    List<XYData> xyItems = xySeries1.getItems();
                    xyItems.forEach(item -> item.setY(RND.nextDouble() * 15));

                    xyItems = xySeries2.getItems();
                    xyItems.forEach(item -> item.setY(RND.nextDouble() * 15));

                    //xyItems = xySeries3.getItems();
                    //xyItems.forEach(item -> item.setY(RND.nextDouble() * 15));

                    xyItems = xySeries4.getItems();
                    xyItems.forEach(item -> item.setY(RND.nextDouble() * 15));

                    List<YData> yItems = ySeries.getItems();
                    yItems.forEach(item -> item.setY(RND.nextDouble() * 20));

                    List<XYZData> xyzItems = xyzSeries.getItems();
                    xyzItems.forEach(item -> item.setZ(RND.nextDouble() * 25));

                    xySeries1.refresh();
                    xySeries2.refresh();
                    //xySeries3.refresh();
                    xySeries4.refresh();
                    ySeries.refresh();
                    xyzSeries.refresh();

                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.add(lineChart, 0, 0);
        gridPane.add(areaChart, 1, 0);
        gridPane.add(smoothLineChart, 0, 1);
        gridPane.add(smoothAreaChart, 1, 1);
        gridPane.add(scatterChart, 0, 2);
        gridPane.add(donutChart, 1, 2);

        Scene scene = new Scene(new StackPane(gridPane));

        stage.setTitle("Charts");
        stage.setScene(scene);
        stage.show();

        timer.start();

        modificationThread.start();
    }

    @Override public void stop() {
        System.exit(0);
    }

    private Axis createLeftYAxis(final double MIN, final double MAX, final boolean AUTO_SCALE) {
        Axis axis = new Axis(Orientation.VERTICAL, Position.LEFT);
        axis.setMinValue(MIN);
        axis.setMaxValue(MAX);
        axis.setPrefWidth(AXIS_WIDTH);
        axis.setAutoScale(AUTO_SCALE);

        AnchorPane.setTopAnchor(axis, 0d);
        AnchorPane.setBottomAnchor(axis, 25d);
        AnchorPane.setLeftAnchor(axis, 0d);

        return axis;
    }
    private Axis createRightYAxis(final double MIN, final double MAX, final boolean AUTO_SCALE) {
        Axis axis = new Axis(Orientation.VERTICAL, Position.RIGHT);
        axis.setMinValue(MIN);
        axis.setMaxValue(MAX);
        axis.setPrefWidth(AXIS_WIDTH);
        axis.setAutoScale(AUTO_SCALE);

        AnchorPane.setRightAnchor(axis, 0d);
        AnchorPane.setTopAnchor(axis, 0d);
        AnchorPane.setBottomAnchor(axis, 25d);

        return axis;
    }

    private Axis createBottomXAxis(final double MIN, final double MAX, final boolean AUTO_SCALE) {
        Axis axis = new Axis(Orientation.HORIZONTAL, Position.BOTTOM);
        axis.setMinValue(MIN);
        axis.setMaxValue(MAX);
        axis.setPrefHeight(AXIS_WIDTH);
        axis.setAutoScale(AUTO_SCALE);

        AnchorPane.setBottomAnchor(axis, 0d);
        AnchorPane.setLeftAnchor(axis, 25d);
        AnchorPane.setRightAnchor(axis, 25d);

        return axis;
    }
    private Axis createTopXAxis(final double MIN, final double MAX, final boolean AUTO_SCALE) {
        Axis axis = new Axis(Orientation.HORIZONTAL, Position.TOP);
        axis.setMinValue(MIN);
        axis.setMaxValue(MAX);
        axis.setPrefHeight(AXIS_WIDTH);
        axis.setAutoScale(AUTO_SCALE);

        AnchorPane.setTopAnchor(axis, 25d);
        AnchorPane.setLeftAnchor(axis, 25d);
        AnchorPane.setRightAnchor(axis, 25d);

        return axis;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
