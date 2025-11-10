package uz.pdp.water_delivery.utils;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class LogErrorFile {

    String logFilePath = "/Users/abdusoburxalimov/Desktop/water_delivery-master/src/main/resources/file/exceptions/log.txt";

    @Transactional
    public void logError(Exception e, String methodName, Integer messageId, Long chatId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentTime = LocalDateTime.now().format(formatter);
        try (FileWriter writer = new FileWriter(logFilePath, true)) {
            writer.write(String.format("| Sana : %-20s | Method : %-20s | Chat ID : %-20s | Xatolik turi : %-20s | Xatolik xabari : %-50s |\n",
                    currentTime, methodName, chatId, e.getClass().getSimpleName(), e.getMessage()));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Transactional
    public void logError(Exception e, String methodName, Long chatId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentTime = LocalDateTime.now().format(formatter);
        try (FileWriter writer = new FileWriter(logFilePath, true)) {
            writer.write(String.format("| Sana : %-20s | Method : %-20s | Chat ID : %-20s | Xatolik turi : %-20s | Xatolik xabari : %-50s |\n",
                    currentTime, methodName, chatId, e.getClass().getSimpleName(), e.getMessage()));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
