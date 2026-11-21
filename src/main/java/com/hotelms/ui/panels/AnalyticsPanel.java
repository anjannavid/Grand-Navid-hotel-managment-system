package com.hotelms.ui.panels;

import com.hotelms.model.Room;
import com.hotelms.service.BookingService;
import com.hotelms.service.RoomService;
import com.hotelms.ui.MainFrame;
import com.hotelms.ui.components.StatCard;
import com.hotelms.ui.components.ThemeManager;
import com.hotelms.utils.FormatUtils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class AnalyticsPanel extends JPanel {

    private final MainFrame mainFrame;
    private final BookingService bookingService;
    private final RoomService roomService;

    private JPanel chartsPanel;
    private StatCard totalBookingsCard;
    private StatCard totalRevenueCard;
    private StatCard avgOccupancyCard;
    private StatCard totalGuestsCard;

    public AnalyticsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.bookingService = new BookingService();
        this.roomService = new RoomService();

        setBackground(ThemeManager.BG_PRIMARY);
        setLayout(new BorderLayout());

        buildUI();
    }

    private void buildUI() {
        JPanel mainContent = new JPanel(new BorderLayout(0, ThemeManager.PADDING_LG));
        mainContent.setOpaque(false);
        mainContent.setBorder(BorderFactory.createEmptyBorder(
            ThemeManager.PADDING_LG, ThemeManager.PADDING_LG,
            ThemeManager.PADDING_LG, ThemeManager.PADDING_LG));

        // Header — standardized
        JButton refreshBtn = ThemeManager.outlineButton("↻  Refresh");
        refreshBtn.addActionListener(e -> refresh());
        JPanel header = ThemeManager.createHeaderPanel("Analytics & Reports", "Key metrics and performance charts", refreshBtn);

        mainContent.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, ThemeManager.PADDING_LG));
        body.setOpaque(false);

        // Stat cards
        JPanel cardsRow = new JPanel(new GridLayout(1, 4, ThemeManager.PADDING_MD, 0));
        cardsRow.setOpaque(false);
        cardsRow.setPreferredSize(new Dimension(0, 110));

        totalBookingsCard = new StatCard("Total Bookings", "0", "All time", ThemeManager.ACCENT_PRIMARY, "📅");
        totalRevenueCard  = new StatCard("Today's Revenue", "৳0", "Revenue today", ThemeManager.ACCENT_SUCCESS, "💰");
        avgOccupancyCard  = new StatCard("Occupancy Rate", "0%", "Current", ThemeManager.ACCENT_WARNING, "📊");
        totalGuestsCard   = new StatCard("Total Guests", "0", "Registered", ThemeManager.ACCENT_INFO, "👥");

        cardsRow.add(totalBookingsCard);
        cardsRow.add(totalRevenueCard);
        cardsRow.add(avgOccupancyCard);
        cardsRow.add(totalGuestsCard);

        body.add(cardsRow, BorderLayout.NORTH);

        // Charts area
        chartsPanel = new JPanel(new GridLayout(1, 2, ThemeManager.PADDING_MD, 0));
        chartsPanel.setOpaque(false);
        chartsPanel.setPreferredSize(new Dimension(0, 420));

        // Wrap chartsPanel so it only takes its preferred height and doesn't stretch to fill the viewport
        JPanel chartsWrapper = new JPanel(new BorderLayout());
        chartsWrapper.setOpaque(false);
        chartsWrapper.add(chartsPanel, BorderLayout.NORTH);

        body.add(chartsWrapper, BorderLayout.CENTER);

        mainContent.add(body, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(mainContent);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(ThemeManager.BG_PRIMARY);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll, BorderLayout.CENTER);
    }

    public void refresh() {
        SwingWorker<AnalyticsData, Void> worker = new SwingWorker<>() {
            @Override protected AnalyticsData doInBackground() {
                AnalyticsData data = new AnalyticsData();
                data.totalBookings = bookingService.countTotal();
                data.todayRevenue = bookingService.getTodayRevenue();
                data.totalRooms = roomService.countTotal();
                data.occupiedRooms = roomService.countOccupied();
                data.totalGuests = new com.hotelms.service.GuestService().countAll();
                data.monthlyRevenue = bookingService.getMonthlyRevenue(LocalDate.now().getYear());
                data.statusCounts = roomService.getStatusCounts();
                return data;
            }

            @Override protected void done() {
                try {
                    AnalyticsData data = get();
                    updateUI(data);
                } catch (Exception e) {
                    mainFrame.showError("Failed to load analytics");
                }
            }
        };
        worker.execute();
    }

    private void updateUI(AnalyticsData data) {
        // Update stat cards
        totalBookingsCard.setValue(String.valueOf(data.totalBookings));
        totalRevenueCard.setValue(FormatUtils.formatCurrency(data.todayRevenue));
        double occRate = data.totalRooms > 0 ? (data.occupiedRooms * 100.0 / data.totalRooms) : 0;
        avgOccupancyCard.setValue(String.format("%.0f%%", occRate));
        avgOccupancyCard.setSubtitle(data.occupiedRooms + " of " + data.totalRooms + " rooms");
        totalGuestsCard.setValue(String.valueOf(data.totalGuests));

        // Rebuild charts
        chartsPanel.removeAll();
        chartsPanel.add(buildRevenueChart(data));
        chartsPanel.add(buildRoomTypeChart(data));
        chartsPanel.revalidate();
        chartsPanel.repaint();
    }

    private JPanel buildRevenueChart(AnalyticsData data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                          "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        // Initialize all months to 0
        for (String m : months) {
            dataset.addValue(0, "Revenue", m);
        }

        // Fill in actual data
        for (double[] entry : data.monthlyRevenue) {
            int monthIdx = (int) entry[0] - 1;
            if (monthIdx >= 0 && monthIdx < 12) {
                dataset.setValue(entry[1], "Revenue", months[monthIdx]);
            }
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Monthly Revenue (" + LocalDate.now().getYear() + ")",
            "Month", "Revenue (৳)",
            dataset, PlotOrientation.VERTICAL, false, true, false);

        styleChart(chart);
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, ThemeManager.ACCENT_PRIMARY);
        renderer.setMaximumBarWidth(0.06);
        chart.setPadding(new org.jfree.chart.ui.RectangleInsets(0, 0, 0, 0));

        // Fix Y-axis scaling if data is empty (avoid 5E-9 scale)
        boolean allZeros = true;
        for (double[] entry : data.monthlyRevenue) {
            if (entry[1] > 0) {
                allZeros = false;
                break;
            }
        }
        org.jfree.chart.axis.NumberAxis rangeAxis = (org.jfree.chart.axis.NumberAxis) plot.getRangeAxis();
        if (allZeros) {
            rangeAxis.setRange(0, 1000); // Set a default scale so it doesn't show 5E-9
        } else {
            rangeAxis.setAutoRangeIncludesZero(true);
        }

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(ThemeManager.BG_CARD);
        chartPanel.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER, 1, true));
        return chartPanel;
    }

    private JPanel buildRoomTypeChart(AnalyticsData data) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

        int standard = 0, deluxe = 0, suite = 0, penthouse = 0;
        // Use room type counts from all rooms
        List<Room> rooms = roomService.getAllRooms();
        for (Room r : rooms) {
            switch (r.getRoomType()) {
                case STANDARD  -> standard++;
                case DELUXE    -> deluxe++;
                case SUITE     -> suite++;
                case PENTHOUSE -> penthouse++;
            }
        }

        if (standard > 0)  dataset.setValue("Standard (" + standard + ")", standard);
        if (deluxe > 0)    dataset.setValue("Deluxe (" + deluxe + ")", deluxe);
        if (suite > 0)     dataset.setValue("Suite (" + suite + ")", suite);
        if (penthouse > 0) dataset.setValue("Penthouse (" + penthouse + ")", penthouse);

        JFreeChart chart = ChartFactory.createPieChart(
            "Room Type Distribution", dataset, true, true, false);

        styleChart(chart);
        PiePlot<?> plot = (PiePlot<?>) chart.getPlot();
        plot.setSectionPaint("Standard (" + standard + ")", ThemeManager.ACCENT_PRIMARY);
        plot.setSectionPaint("Deluxe (" + deluxe + ")", ThemeManager.ACCENT_SUCCESS);
        plot.setSectionPaint("Suite (" + suite + ")", ThemeManager.ACCENT_WARNING);
        plot.setSectionPaint("Penthouse (" + penthouse + ")", ThemeManager.ACCENT_DANGER);
        plot.setLabelBackgroundPaint(ThemeManager.BG_TERTIARY);
        plot.setLabelPaint(ThemeManager.TEXT_PRIMARY);
        plot.setLabelFont(ThemeManager.FONT_SMALL);
        plot.setInteriorGap(0.02); // Reduce gap between pie and border
        chart.setPadding(new org.jfree.chart.ui.RectangleInsets(0, 0, 0, 0)); // Remove extra chart padding

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(ThemeManager.BG_CARD);
        chartPanel.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER, 1, true));
        return chartPanel;
    }

    private void styleChart(JFreeChart chart) {
        chart.setBackgroundPaint(new Color(ThemeManager.BG_CARD.getRGB()));
        chart.getTitle().setPaint(ThemeManager.TEXT_PRIMARY);
        chart.getTitle().setFont(ThemeManager.FONT_H3);

        if (chart.getPlot() instanceof CategoryPlot cp) {
            cp.setBackgroundPaint(ThemeManager.BG_PRIMARY);
            cp.setOutlinePaint(ThemeManager.BORDER);
            cp.setRangeGridlinePaint(ThemeManager.BORDER);
            cp.getDomainAxis().setTickLabelPaint(ThemeManager.TEXT_SECONDARY);
            cp.getDomainAxis().setTickLabelFont(ThemeManager.FONT_SMALL);
            cp.getDomainAxis().setLabelPaint(ThemeManager.TEXT_SECONDARY);
            cp.getRangeAxis().setTickLabelPaint(ThemeManager.TEXT_SECONDARY);
            cp.getRangeAxis().setTickLabelFont(ThemeManager.FONT_SMALL);
            cp.getRangeAxis().setLabelPaint(ThemeManager.TEXT_SECONDARY);
        } else if (chart.getPlot() instanceof PiePlot<?> pp) {
            pp.setBackgroundPaint(ThemeManager.BG_PRIMARY);
            pp.setOutlinePaint(ThemeManager.BORDER);
            pp.setShadowPaint(null);
        }

        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(ThemeManager.BG_CARD);
            chart.getLegend().setItemPaint(ThemeManager.TEXT_SECONDARY);
            chart.getLegend().setItemFont(ThemeManager.FONT_SMALL);
        }
    }

    private static class AnalyticsData {
        int totalBookings;
        double todayRevenue;
        int totalRooms;
        int occupiedRooms;
        int totalGuests;
        List<double[]> monthlyRevenue;
        Map<Room.RoomStatus, Integer> statusCounts;
    }
}
