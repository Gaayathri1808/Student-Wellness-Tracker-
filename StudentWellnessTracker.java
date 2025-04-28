import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class StudentWellnessTracker {

    static class WellnessActivity implements Serializable {
        private static final long serialVersionUID = 1L;
        private String type;
        private int duration;
        private Date date;
        private String notes;

        public WellnessActivity(String type, int duration, Date date, String notes) {
            this.type = type;
            this.duration = duration;
            this.date = date;
            this.notes = notes;
        }

        public String getType() {
            return type;
        }

        public int getDuration() {
            return duration;
        }

        public Date getDate() {
            return date;
        }

        public String getNotes() {
            return notes;
        }

        @Override
        public String toString() {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            return type + " | Duration: " + duration + " mins | Date: " + sdf.format(date) + " | Notes: " + notes;
        }
    }

    static class ActivityManager implements Serializable {
        private static final long serialVersionUID = 1L;
        private List<WellnessActivity> activities = new ArrayList<>();

        public void addActivity(String type, int duration, Date date, String notes) {
            activities.add(new WellnessActivity(type, duration, date, notes));
        }

        public void updateActivity(int index, String type, int duration, Date date, String notes) throws DataNotFoundException {
            if (index < 0 || index >= activities.size()) {
                throw new DataNotFoundException("Activity not found.");
            }
            WellnessActivity activity = activities.get(index);
            activity = new WellnessActivity(type, duration, date, notes);
            activities.set(index, activity);
        }

        public void deleteActivity(int index) throws DataNotFoundException {
            if (index < 0 || index >= activities.size()) {
                throw new DataNotFoundException("Activity not found.");
            }
            activities.remove(index);
        }

        public List<WellnessActivity> getActivities() {
            return activities;
        }

        public void viewActivities() {
            if (activities.isEmpty()) {
                System.out.println("No activities recorded.");
            } else {
                for (int i = 0; i < activities.size(); i++) {
                    System.out.println(i + ": " + activities.get(i));
                }
            }
        }

        public void processActivitiesAsStream() {
            if (activities.isEmpty()) {
                System.out.println("No activities to process.");
                return;
            }

            System.out.println("\nProcessing activities using stream:");
            System.out.println("\nExercise Activities:");
            activities.stream()
                    .filter(activity -> activity.getType().equalsIgnoreCase("Exercise"))
                    .forEach(System.out::println);

            int totalDuration = activities.stream()
                    .mapToInt(WellnessActivity::getDuration)
                    .sum();
            System.out.println("\nTotal Duration of all activities: " + totalDuration + " minutes");

            Map<String, List<WellnessActivity>> activitiesByType = activities.stream()
                    .collect(Collectors.groupingBy(WellnessActivity::getType));
            System.out.println("\nActivities grouped by type:");
            activitiesByType.forEach((type, activityList) -> {
                System.out.println(type + ":");
                activityList.forEach(System.out::println);
            });
        }
    }

    static class DataNotFoundException extends Exception {
        public DataNotFoundException(String message) {
            super(message);
        }
    }

    static class DataManager {
        private static final String DATA_FILE = "wellness_data.txt";
        private static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        public static void saveActivityData(ActivityManager activityManager) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_FILE))) {
                List<WellnessActivity> activities = activityManager.getActivities();
                for (WellnessActivity activity : activities) {
                    // Format: type,duration,date,notes
                    String data = activity.getType() + "," + activity.getDuration() + "," + sdf.format(activity.getDate()) + "," + activity.getNotes();
                    writer.println(data);
                }
                System.out.println("Activity data saved successfully to " + DATA_FILE);
            } catch (IOException e) {
                System.err.println("Error saving activity data: " + e.getMessage());
            }
        }

        public static ActivityManager loadActivityData() {
            ActivityManager activityManager = new ActivityManager();
            File file = new File(DATA_FILE);
            if (!file.exists()) {
                System.out.println("No existing data found. Returning a new ActivityManager.");
                return activityManager;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length == 4) {
                        String type = data[0];
                        int duration = Integer.parseInt(data[1]);
                        Date date = sdf.parse(data[2]);
                        String notes = data[3];
                        activityManager.addActivity(type, duration, date, notes);
                    } else {
                        System.err.println("Skipping invalid line: " + line);
                    }
                }
                System.out.println("Activity data loaded successfully from " + DATA_FILE);
            } catch (IOException | ParseException e) {
                System.err.println("Error loading activity data: " + e.getMessage());
                return new ActivityManager();
            }
            return activityManager;
        }
    }

    private static ActivityManager activityManager;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        activityManager = DataManager.loadActivityData();

        while (true) {
            showMenu();
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    addWellnessActivity();
                    break;
                case 2:
                    viewActivities();
                    break;
                case 3:
                    updateActivity();
                    break;
                case 4:
                    deleteActivity();
                    break;
                case 5:
                    activityManager.processActivitiesAsStream();
                    break;
                case 6:
                    DataManager.saveActivityData(activityManager);
                    System.out.println("Exiting the application...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void showMenu() {
        System.out.println("\nStudent Wellness Tracker");
        System.out.println("1. Add Wellness Activity");
        System.out.println("2. View Wellness Activities");
        System.out.println("3. Update Wellness Activity");
        System.out.println("4. Delete Wellness Activity");
        System.out.println("5. Process Activities (Stream)");
        System.out.println("6. Exit");
        System.out.print("Enter your choice: ");
    }

    private static void addWellnessActivity() {
        try {
            System.out.print("Enter Activity Type (Exercise, Study, Meditation, etc.): ");
            String type = scanner.nextLine();

            System.out.print("Enter Duration (in minutes): ");
            int duration = scanner.nextInt();
            scanner.nextLine();

            System.out.print("Enter Date (dd.MM.yyyy): ");
            String dateStr = scanner.nextLine();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            Date date = sdf.parse(dateStr);

            System.out.print("Enter Notes: ");
            String notes = scanner.nextLine();

            activityManager.addActivity(type, duration, date, notes);
            System.out.println("Activity added successfully!");
        } catch (ParseException e) {
            System.out.println("Error parsing date: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error adding activity: " + e.getMessage());
        }
    }

    private static void viewActivities() {
        activityManager.viewActivities();
    }

    private static void updateActivity() {
        try {
            System.out.print("Enter Activity Index to Update: ");
            int index = scanner.nextInt();
            scanner.nextLine();

            System.out.print("Enter New Activity Type: ");
            String type = scanner.nextLine();

            System.out.print("Enter New Duration (in minutes): ");
            int duration = scanner.nextInt();
            scanner.nextLine();

            System.out.print("Enter New Date (dd.MM.yyyy): ");
            String dateStr = scanner.nextLine();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            Date date = sdf.parse(dateStr);

            System.out.print("Enter New Notes: ");
            String notes = scanner.nextLine();

            activityManager.updateActivity(index, type, duration, date, notes);
            System.out.println("Activity updated successfully!");
        } catch (ParseException e) {
            System.out.println("Error parsing date: " + e.getMessage());
        } catch (DataNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("Error updating activity: " + e.getMessage());
        }
    }

    private static void deleteActivity() {
        try {
            System.out.print("Enter Activity Index to Delete: ");
            int index = scanner.nextInt();
            scanner.nextLine();
            activityManager.deleteActivity(index);
            System.out.println("Activity deleted successfully!");
        } catch (DataNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }
}
