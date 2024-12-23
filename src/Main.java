import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

// Interface for CRUD operations
interface OperasiCrud {
    void tambah();
    void lihat();
    void perbarui();
    void hapus();
}

// User class
class Pengguna {
    protected String username;
    protected String password;

    public Pengguna(String username, String password) {
        this.username = username;
        this.password = password;
    }
}

// Admin subclass
class Admin extends Pengguna {
    public Admin(String username, String password) {
        super(username, password);
    }

    public boolean login(String inputUsername, String inputPassword) {
        return this.username.equals(inputUsername) && this.password.equals(inputPassword);
    }
}

// Inventory Manager implementing CRUD operations
class ManajerInventaris implements OperasiCrud {
    private final Connection connection;
    private final Scanner scanner;

    public ManajerInventaris(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    @Override
    public void tambah() {
        System.out.print("Masukkan ID buah: ");
        if (scanner.hasNextInt()) {
            int id = scanner.nextInt();
            scanner.nextLine(); // Clear newline after nextInt()
            System.out.print("Masukkan nama buah: ");
            String nama = scanner.nextLine();
            System.out.print("Masukkan berat (dalam kg): ");
            if (scanner.hasNextDouble()) {
                double berat = scanner.nextDouble();
                scanner.nextLine(); // Clear newline after nextDouble()
                String sql = "INSERT INTO buah (id, nama_buah, berat_buah) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, id);
                    stmt.setString(2, nama);
                    stmt.setDouble(3, berat);
                    stmt.executeUpdate();
                    System.out.println("Item berhasil ditambahkan!");
                } catch (SQLException e) {
                    System.out.println("Kesalahan database: " + e.getMessage());
                }
            } else {
                System.out.println("Input berat tidak valid.");
                scanner.nextLine(); // Clear invalid input
            }
        } else {
            System.out.println("Input ID tidak valid.");
            scanner.nextLine(); // Clear invalid input
        }
    }

    @Override
    public void lihat() {
        String sql = "SELECT * FROM buah";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("+-------+-----------------+-------------+");
            System.out.println("| ID    | Nama Buah       | Berat (kg)  |");
            System.out.println("+-------+-----------------+-------------+");
            while (rs.next()) {
                int id = rs.getInt("id");
                String nama = rs.getString("nama_buah");
                double berat = rs.getDouble("berat_buah");
                System.out.printf("| %-5d | %-15s | %-10.2f kg |\n", id, nama, berat);
            }
            System.out.println("+-------+-----------------+-------------+");
        } catch (SQLException e) {
            System.out.println("Kesalahan database: " + e.getMessage());
        }
    }

    @Override
    public void perbarui() {
        System.out.print("Masukkan ID buah yang ingin diperbarui: ");
        if (scanner.hasNextInt()) {
            int id = scanner.nextInt();
            scanner.nextLine(); // Clear newline after nextInt()
            System.out.print("Masukkan berat baru (dalam kg): ");
            if (scanner.hasNextDouble()) {
                double berat = scanner.nextDouble();
                scanner.nextLine(); // Clear newline after nextDouble()
                String sql = "UPDATE buah SET berat_buah = ? WHERE id = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setDouble(1, berat);
                    stmt.setInt(2, id);
                    int rowsUpdated = stmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        System.out.println("Item berhasil diperbarui!");
                    } else {
                        System.out.println("Item dengan ID tersebut tidak ditemukan.");
                    }
                } catch (SQLException e) {
                    System.out.println("Kesalahan database: " + e.getMessage());
                }
            } else {
                System.out.println("Input berat tidak valid.");
                scanner.nextLine(); // Clear invalid input
            }
        } else {
            System.out.println("Input ID tidak valid.");
            scanner.nextLine(); // Clear invalid input
        }
    }

    @Override
    public void hapus() {
        System.out.print("Masukkan ID buah yang ingin dihapus: ");
        if (scanner.hasNextInt()) {
            int id = scanner.nextInt();
            scanner.nextLine(); // Clear newline after nextInt()
            String sql = "DELETE FROM buah WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, id);
                int rowsDeleted = stmt.executeUpdate();
                if (rowsDeleted > 0) {
                    System.out.println("Item berhasil dihapus!");
                } else {
                    System.out.println("Item dengan ID tersebut tidak ditemukan.");
                }
            } catch (SQLException e) {
                System.out.println("Kesalahan database: " + e.getMessage());
            }
        } else {
            System.out.println("Input ID tidak valid.");
            scanner.nextLine(); // Clear invalid input
        }
    }

    // New method to count the number of fruit types
    public void hitungJenisBuah() {
        String sql = "SELECT COUNT(*) AS jumlah FROM buah";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int jumlah = rs.getInt("jumlah");
                System.out.println("Jumlah jenis buah yang tersedia: " + jumlah);
            }
        } catch (SQLException e) {
            System.out.println("Kesalahan database: " + e.getMessage());
        }
    }
}

// Class to handle database connection
class DatabaseConnection {
    public static final String DB_URL = "jdbc:postgresql://localhost:5433/TBPBO";
    public static final String DB_USER = "postgres";
    public static final String DB_PASSWORD = "101104";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            System.err.println("Koneksi ke database gagal: " + e.getMessage());
            return null;
        }
    }
}

// Main program class
public class Main {
    public static void main(String[] args) {
        try (Connection connection = DatabaseConnection.getConnection(); Scanner scanner = new Scanner(System.in)) {
            if (connection == null) {
                System.out.println("Tidak dapat terhubung ke database.");
                return;
            }

            Admin admin = new Admin("admin", "123");
            ManajerInventaris manajer = new ManajerInventaris(connection, scanner);

            // User login process
            System.out.print("Masukkan username: ");
            String username = scanner.nextLine();
            System.out.print("Masukkan password: ");
            String password = scanner.nextLine();

            if (admin.login(username, password)) {
                LocalDateTime sekarang = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String tanggalTerkini = sekarang.format(formatter);
                System.out.println("Login berhasil! Selamat datang, " + username + ". Login pada: " + tanggalTerkini);

                while (true) {
                    // Menu options for inventory management
                    System.out.println("\n1. Tambah Item\n2. Lihat Item\n3. Perbarui Item\n4. Hapus Item\n5. Hitung Jenis Buah\n6. Keluar");
                    System.out.print("Pilih opsi: ");

                    if (scanner.hasNextInt()) {
                        int pilihan = scanner.nextInt();
                        scanner.nextLine(); // Clear newline after nextInt()

                        switch (pilihan) {
                            case 1 -> manajer.tambah();
                            case 2 -> manajer.lihat();
                            case 3 -> manajer.perbarui();
                            case 4 -> manajer.hapus();
                            case 5 -> manajer.hitungJenisBuah();
                            case 6 -> {
                                System.out.println("Keluar... Sampai jumpa!");
                                return;
                            }
                            default -> System.out.println("Pilihan tidak valid. Coba lagi.");
                        }
                    } else {
                        System.out.println("Input tidak valid. Harap masukkan angka.");
                        scanner.nextLine(); // Clear invalid input
                    }
                }
            } else {
                System.out.println("Username atau password salah.");
            }

        } catch (SQLException e) {
            System.out.println("Kesalahan koneksi: " + e.getMessage());
        }
    }
}
