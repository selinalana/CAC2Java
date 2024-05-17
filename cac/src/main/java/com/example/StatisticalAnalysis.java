package com.example;

import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.stat.inference.OneWayAnova;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticalAnalysis {

    public static void main(String[] args) {
        // Loading dataset
        List<String[]> dataset = loadDataset("cac\\src\\main\\java\\com\\example\\dataset\\mlb_salaries.csv");

        // Extracting salary and batting average data
        double[] salaries = extractColumn(dataset, 11); // Assuming salary is in column index 11
        double[] battingAverages = extractColumn(dataset, 2); // Assuming batting average is in column index 2

        // Perform ANOVA
        List<double[]> dataGroups = new ArrayList<>();
        dataGroups.add(salaries);
        dataGroups.add(battingAverages);

        double pValue = performAnova(dataGroups);
        System.out.println("ANOVA p-value: " + pValue);

        // Interpret ANOVA result
        if (pValue < 0.05) {
            System.out.println("There is a significant difference between the groups.");
        } else {
            System.out.println("There is no significant difference between the groups.");
        }

        // Calculate and display mean, median, and mode for salaries and batting averages
        System.out.println("Salary Statistics:");
        displayStatistics(salaries);

        System.out.println("Batting Average Statistics:");
        displayStatistics(battingAverages);

        // Plot graphs
        plotHistogram(salaries, "Salary Distribution", "Salaries");
        plotHistogram(battingAverages, "Batting Average Distribution", "Batting Averages");
        plotScatterPlot(salaries, battingAverages, "Salaries vs Batting Averages", "Salaries", "Batting Averages");
        plotLineChart(new double[]{calculateMean(salaries), calculateMedian(salaries), calculateMode(salaries)},
                      new double[]{calculateMean(battingAverages), calculateMedian(battingAverages), calculateMode(battingAverages)});
    }

    // Method to load dataset from CSV file
    public static List<String[]> loadDataset(String filePath) {
        List<String[]> data = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] row = line.split(",");
                data.add(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    // Method to extract a column from the dataset, filtering out non-numeric values
    public static double[] extractColumn(List<String[]> dataset, int columnIndex) {
        List<Double> columnDataList = new ArrayList<>();

        for (int i = 1; i < dataset.size(); i++) { // Start from 1 to skip header
            try {
                String value = dataset.get(i)[columnIndex];
                columnDataList.add(Double.parseDouble(value));
            } catch (NumberFormatException e) {
                System.err.println("Error parsing value in row " + i + ", column " + columnIndex);
                // Ignore invalid values
            }
        }

        double[] columnData = new double[columnDataList.size()];
        for (int i = 0; i < columnDataList.size(); i++) {
            columnData[i] = columnDataList.get(i);
        }

        return columnData;
    }

    // Method to perform ANOVA
    public static double performAnova(List<double[]> dataGroups) {
        OneWayAnova anova = new OneWayAnova();
        return anova.anovaPValue(dataGroups);
    }

    // Method to calculate mean
    public static double calculateMean(double[] data) {
        double sum = 0;
        for (double num : data) {
            sum += num;
        }
        return sum / data.length;
    }

    // Method to calculate median
    public static double calculateMedian(double[] data) {
        Median median = new Median();
        return median.evaluate(data);
    }

    // Method to calculate mode
    public static double calculateMode(double[] data) {
        Map<Double, Integer> frequencyMap = new HashMap<>();
        for (double num : data) {
            frequencyMap.put(num, frequencyMap.getOrDefault(num, 0) + 1);
        }

        double mode = Double.NaN;
        int maxCount = -1;
        for (Map.Entry<Double, Integer> entry : frequencyMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mode = entry.getKey();
            }
        }

        return mode;
    }

    // Method to display statistics
    public static void displayStatistics(double[] data) {
        System.out.println("Mean: " + calculateMean(data));
        System.out.println("Median: " + calculateMedian(data));
        System.out.println("Mode: " + calculateMode(data));
    }

    // Method to plot histogram
    public static void plotHistogram(double[] data, String title, String xAxisLabel) {
        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries("Histogram", data, 50);

        JFreeChart histogram = ChartFactory.createHistogram(
                title,
                xAxisLabel,
                "Frequency",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        ChartPanel chartPanel = new ChartPanel(histogram);
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }

    // Method to plot scatter plot
    public static void plotScatterPlot(double[] xData, double[] yData, String title, String xAxisLabel, String yAxisLabel) {
        XYSeries series = new XYSeries("Scatter Plot");
        for (int i = 0; i < xData.length; i++) {
            series.add(xData[i], yData[i]);
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart scatterPlot = ChartFactory.createScatterPlot(
                title,
                xAxisLabel,
                yAxisLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        ChartPanel chartPanel = new ChartPanel(scatterPlot);
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }

    // Method to plot line chart for mean, median, and mode
    public static void plotLineChart(double[] salaryStats, double[] battingAverageStats) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String[] stats = {"Mean", "Median", "Mode"};

        for (int i = 0; i < stats.length; i++) {
            dataset.addValue(salaryStats[i], "Salaries", stats[i]);
            dataset.addValue(battingAverageStats[i], "Batting Averages", stats[i]);
        }

        JFreeChart lineChart = ChartFactory.createLineChart(
                "Statistics Comparison",
                "Statistic",
                "Value",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        XYPlot plot = lineChart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        plot.setRenderer(renderer);

        ChartPanel chartPanel = new ChartPanel(lineChart);
        JFrame frame = new JFrame("Statistics Comparison");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }
}
