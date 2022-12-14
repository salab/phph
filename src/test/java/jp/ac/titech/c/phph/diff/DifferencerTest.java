package jp.ac.titech.c.phph.diff;

import org.eclipse.jgit.diff.EditList;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DifferencerTest {
    @Test
    public void testDifferencer() throws IOException {
        List<String> lines1 = load("expand1.c");
        assertEquals(30, lines1.size());

        List<String> lines2 = load("expand2.c");
        assertEquals(37, lines2.size());

        final Differencer<String> myers = JGitDifferencer.newMyers();
        final EditList mEdits = myers.compute(lines1, lines2);
        assertEquals(6, mEdits.size());
        assertEquals("INSERT(1-1,1-2)", mEdits.get(0).toString());
        assertEquals("DELETE(2-3,3-3)", mEdits.get(1).toString());
        assertEquals("REPLACE(9-14,9-10)", mEdits.get(2).toString());
        assertEquals("INSERT(19-19,15-19)", mEdits.get(3).toString());
        assertEquals("REPLACE(20-24,20-33)", mEdits.get(4).toString());
        assertEquals("DELETE(25-27,34-34)", mEdits.get(5).toString());

        final Differencer<String> dp = new DynamicProgrammingDifferencer<>();
        final EditList dEdits = dp.compute(lines1, lines2);
        assertEquals(7, dEdits.size());
        assertEquals("INSERT(1-1,1-2)", dEdits.get(0).toString());
        assertEquals("DELETE(2-3,3-3)", dEdits.get(1).toString());
        assertEquals("REPLACE(9-14,9-10)", dEdits.get(2).toString());
        assertEquals("INSERT(18-18,14-17)", dEdits.get(3).toString());
        assertEquals("INSERT(19-19,18-19)", dEdits.get(4).toString());
        assertEquals("REPLACE(20-24,20-33)", dEdits.get(5).toString());
        assertEquals("DELETE(25-27,34-34)", dEdits.get(6).toString());
    }

    private static List<String> load(final String filename) throws IOException {
        return Files.readAllLines(getPath(filename));
    }

    private static Path getPath(final String filename) {
        return Path.of(DifferencerTest.class.getClassLoader().getResource(filename).getPath());
    }
}
