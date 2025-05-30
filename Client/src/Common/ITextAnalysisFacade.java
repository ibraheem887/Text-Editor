package Common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import javax.swing.JFrame;

public interface ITextAnalysisFacade extends Remote {

    // File Management
    void createFile(String title, String content) throws RemoteException;
    void deleteFile(String title)throws RemoteException;
    String getFileContent(String title)throws RemoteException;
    void updateFile(String title, String content)throws RemoteException;
    List<String[]> getAllFilesWithDates()throws RemoteException;
    

    // Lemmatization
    String lemmatizeText(String text)throws RemoteException;
    String lemmatizeSentence(String sentence)throws RemoteException;
    void saveLemmatizationResult(String title, String originalText, String lemmatizedText)throws RemoteException;
    List<String[]> getLemmatizationResults(int fileId)throws RemoteException;

    // POS Tagging
    List<String> segmentArabicText(String text)throws RemoteException;
    List<String[]> tagWords(List<String> words)throws RemoteException;
    void displayPOSResults(List<String[]> taggedWords)throws RemoteException;

    // TF-IDF, PMI, PKL Analysis
    void calculateTfIdf(String arabicText)throws RemoteException;
    void calculatePmi(String arabicText)throws RemoteException;
    void calculatePkl(String arabicText)throws RemoteException;

    // High-level operation: Analyze a file and perform POS tagging and lemmatization
    void analyzeFile(String fileTitle, JFrame parentFrame)throws RemoteException;

    // Save POS Tagging Results
    void savePOSTaggingResults(String title, List<String[]> posTaggedWords)throws RemoteException;

    // Transliteration
    String transliterate(String arabicContent)throws RemoteException;
	int getFileIdByTitle(String currentFileTitle)throws RemoteException;
	boolean fileExists(String title)throws RemoteException;
}
