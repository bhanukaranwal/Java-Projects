import javax.sound.midi.*;
import java.util.Random;

public class ProceduralMusicGenerator {
    private static final int TEMPO_BPM = 120;
    private static final int VELOCITY = 80;
    private static final int INSTRUMENT = 41; // Violin

    private static final Random random = new Random();

    private static final int[] SCALE = {60, 62, 64, 65, 67, 69, 71, 72}; // C major scale (MIDI note numbers)
    private static final int NOTE_DURATION = 240; // ticks, about a quarter note at 120 BPM

    private Sequencer sequencer;
    private Sequence sequence;
    private Track track;

    public ProceduralMusicGenerator() throws Exception {
        sequencer = MidiSystem.getSequencer();
        sequencer.open();
        sequence = new Sequence(Sequence.PPQ, 480);
        track = sequence.createTrack();

        // Set instrument
        ShortMessage instrumentChange = new ShortMessage();
        instrumentChange.setMessage(ShortMessage.PROGRAM_CHANGE, 0, INSTRUMENT, 0);
        track.add(new MidiEvent(instrumentChange, 0));

        generateMusicPattern();

        sequencer.setSequence(sequence);
        sequencer.setTempoInBPM(TEMPO_BPM);
        sequencer.start();
    }

    private void generateMusicPattern() throws Exception {
        int tick = 0;

        // Generate a sequence of notes
        for (int i = 0; i < 64; i++) {
            int note = SCALE[random.nextInt(SCALE.length)];
            int length = NOTE_DURATION;

            addNoteOnOff(note, tick, length);

            tick += length;
            // Occasionally vary note duration or insert rests
            if (random.nextDouble() < 0.2) {
                tick += NOTE_DURATION; // rest for a beat
            }
        }

        // Add an end of track meta event at the end
        MetaMessage metaEnd = new MetaMessage();
        byte[] empty = {};
        metaEnd.setMessage(0x2F, empty, 0);
        track.add(new MidiEvent(metaEnd, tick + 1));
    }

    private void addNoteOnOff(int note, int startTick, int duration) throws Exception {
        ShortMessage on = new ShortMessage();
        on.setMessage(ShortMessage.NOTE_ON, 0, note, VELOCITY);
        track.add(new MidiEvent(on, startTick));

        ShortMessage off = new ShortMessage();
        off.setMessage(ShortMessage.NOTE_OFF, 0, note, 0);
        track.add(new MidiEvent(off, startTick + duration));
    }

    public static void main(String[] args) {
        System.out.println("Procedural Music Generator - playing generated music...");
        try {
            ProceduralMusicGenerator generator = new ProceduralMusicGenerator();

            // Keep program running while music plays
            while (generator.sequencer.isRunning()) {
                Thread.sleep(100);
            }

            generator.sequencer.close();
            System.out.println("Music finished.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
