"""Exercise the snapshot against real class files and public API changes."""

import struct
import subprocess
import tempfile
import unittest
from pathlib import Path

from api_snapshot import class_access_offset, class_names, snapshot


class ApiSnapshotTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)

    def compile(self, extra="", private_body="return 1;"):
        source = self.root / "Api.java"
        source.write_text("""
            package fixture;
            public class Api {
                public enum Choice { A, B }
                public static class Companion {}
                private static class Hidden {}
                public static final long LONG = 42L;
                public static final double DOUBLE = 1.5;
                public static final String TEXT = "constant";
                public void call() {}
                public static void call$default(Api receiver, int mask) {}
                public static int access$helper() { return 1; }
                protected void extensionPoint() {}
                private int helper() { PRIVATE_BODY }
                EXTRA
            }
            class Implementation {}
        """.replace("EXTRA", extra).replace("PRIVATE_BODY", private_body))
        subprocess.run(["javac", "-d", str(self.root), str(source)], check=True)

    def test_filters_implementation_but_retains_nested_and_default_abi(self):
        self.compile()
        synthetic = self.root / "Synthetic.java"
        synthetic.write_text("package fixture; public class Synthetic {}")
        compose = self.root / "ComposableSingletons$ApiKt.java"
        compose.write_text("package fixture; public class ComposableSingletons$ApiKt {}")
        inline = self.root / "ApiKt$block$1.java"
        inline.write_text("package fixture; public class ApiKt$block$1 {}")
        subprocess.run(["javac", "-d", str(self.root), str(synthetic), str(compose), str(inline)], check=True)
        path = self.root / "fixture/Synthetic.class"
        data = bytearray(path.read_bytes())
        offset = class_access_offset(data)
        flags = struct.unpack_from(">H", data, offset)[0]
        struct.pack_into(">H", data, offset, flags | 0x1000)
        path.write_bytes(data)

        self.assertEqual(class_names([self.root]),
                         ["fixture.Api", "fixture.Api$Choice", "fixture.Api$Companion"])
        result = snapshot(str(self.root), [self.root])
        self.assertIn("call$default", result)
        self.assertIn("extensionPoint", result)
        self.assertIn("fixture.Api$Choice", result)
        self.assertNotIn("access$helper", result)
        self.assertNotIn("helper()", result)

    def test_public_change_changes_snapshot_private_change_does_not(self):
        self.compile()
        before = snapshot(str(self.root), [self.root])
        self.compile(private_body="return 2;")
        self.assertEqual(before, snapshot(str(self.root), [self.root]))
        self.compile(extra="public void newlyAdded() {}")
        self.assertNotEqual(before, snapshot(str(self.root), [self.root]))
        self.assertIn("newlyAdded", snapshot(str(self.root), [self.root]))


if __name__ == "__main__":
    unittest.main()
