package pocketsphinx;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PocketSphinxlm {

    public static void main(String[] args) {

        PocketSphinx sphinx = new PocketSphinx();

        // Initialize PocketSphinx
        long decoderPtr = sphinx.initialize_lm("model/en-us/en-us", "model/en-us/en-us.lm.bin", "model/en-us/cmudict-en-us.dict");
        if (decoderPtr == 0) {
            System.err.println("Failed to initialize PocketSphinx");
            return;
        }

        // Start listening
        sphinx.startListening(decoderPtr);
        System.out.println("Listening... Speak now!");

        // Configure microphone
        try {
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();

            int ms = 3000;
            int SIZE = ms*16000*2/1000; // 16kHz x 2 bytes per sample /1000 ms per second
            byte[] buffer = new byte[SIZE]; 
            
                int bytesRead = microphone.read(buffer, 0, buffer.length);

                System.out.println("Mic finished");

                sphinx.processAudio(decoderPtr, buffer, bytesRead);
                System.out.println("Audio processed");
                // sphinx.processAudio(decoderPtr, shortBuffer, bytesRead*2);

                // String partialResult = sphinx.getPartialResult(decoderPtr);
                // if (!partialResult.isEmpty()) {
                //     System.out.println("Partial: " + partialResult);
                // }
                RecognitionResult result = sphinx.getRecognitionHypothesis(decoderPtr);
                System.out.println("Partial: " + result.result+" "+result.score);
            

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

