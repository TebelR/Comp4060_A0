package pocketsphinx;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PocketSphinxkws {

    public static void main(String[] args) {
        PocketSphinx sphinx = new PocketSphinx();

        // Initialize PocketSphinx
        long decoderPtr = sphinx.initialize_kws("model/en-us/en-us", "keyphrases.txt", "model/en-us/cmudict-en-us.dict");
        if (decoderPtr == 0) {
            System.err.println("Failed to initialize PocketSphinx");
            return;
        }

        // Start listening
        sphinx.startListening(decoderPtr);
        System.out.println("Decoder Started");

        // Configure microphone
        try {
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();
            System.out.println("Microphone started... Speak now!");

            boolean keywordDetected = false;

            int ms = 256;
            int SIZE = ms*16000*2/1000; // 16kHz x 2 bytes per sample /1000 ms per second
            byte[] buffer = new byte[SIZE]; 
            while (!keywordDetected) {
            
                int bytesRead = microphone.read(buffer, 0, buffer.length);
                sphinx.processAudio(decoderPtr, buffer, bytesRead);
             
                // String partialResult = sphinx.getPartialResult(decoderPtr);
                // if (!partialResult.isEmpty()) {
                //     System.out.println("Partial: " + partialResult);
                // }
                RecognitionResult result = sphinx.getRecognitionHypothesis(decoderPtr);
                System.out.println("Partial: " + result.result+" "+result.score);
                if (!result.result.isEmpty()) {
                  System.out.println("Partial: " + result.result+" "+result.score);
                  keywordDetected = true;
                }     
            }     

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Stop listening
        sphinx.stopListening(decoderPtr);
        // String finalResult = sphinx.getFinalResult(decoderPtr);
        // System.out.println("Final result: " + finalResult);
        RecognitionResult result = sphinx.getRecognitionHypothesis(decoderPtr);
        System.out.println("Final: " + result.result+" "+result.score);

        // Cleanup
        sphinx.cleanup(decoderPtr);
    }
}

