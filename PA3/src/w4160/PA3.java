package w4160;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import org.lwjgl.util.glu.*;
import org.lwjgl.util.vector.Vector3f;
import utility.BufferTools;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;


public class PA3 {

    String windowTitle = "3D Shapes";
    public boolean closeRequested = false;

    long lastFrameTime; // used to calculate delta
    
    float triangleAngle; // Angle of rotation for the triangles
    float quadAngle; // Angle of rotation for the quads

    ShaderProgram shader,blinn,checker;
    
    public void run() {

        createWindow();
        getDelta(); // Initialise delta timer
        initGL();
        initShaders();
        setUpLighting();
        
        while (!closeRequested) {
            pollInput();
            updateLogic(getDelta());
            renderGL();

            Display.update();
        }
        
        cleanup();
    }
    
    private void initGL() {

        /* OpenGL */
        int width = Display.getDisplayMode().getWidth();
        int height = Display.getDisplayMode().getHeight();

        GL11.glViewport(0, 0, width, height); // Reset The Current Viewport
        GL11.glMatrixMode(GL11.GL_PROJECTION); // Select The Projection Matrix
        GL11.glLoadIdentity(); // Reset The Projection Matrix
        GLU.gluPerspective(45.0f, ((float) width / (float) height), 0.1f, 100.0f); // Calculate The Aspect Ratio Of The Window
        GL11.glMatrixMode(GL11.GL_MODELVIEW); // Select The Modelview Matrix
        GL11.glLoadIdentity(); // Reset The Modelview Matrix

        GL11.glShadeModel(GL11.GL_SMOOTH); // Enables Smooth Shading
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // Black Background
        GL11.glClearDepth(1.0f); // Depth Buffer Setup
        GL11.glEnable(GL11.GL_DEPTH_TEST); // Enables Depth Testing
        GL11.glDepthFunc(GL11.GL_LEQUAL); // The Type Of Depth Test To Do
        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST); // Really Nice Perspective Calculations
        Camera.create();        
    }
    
    private void initShaders() {


        try {
            shader = new ShaderProgram("res/phong_reflection.vs", "res/gourand_shading.fs");
            blinn = new ShaderProgram("res/blinn_phong.vs", "res/gourand_shading.fs");
            checker = new ShaderProgram("res/checkerboard.vs", "res/checkerboard.fs");
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private void updateLogic(int delta) {
        triangleAngle += 0.1f * delta; // Increase The Rotation Variable For The Triangles
        quadAngle -= 0.05f * delta; // Decrease The Rotation Variable For The Quads
    }

    private void renderGL() {

        // start to use shaders
        shader.begin();
        float dir = (float)(1./Math.sqrt(3));
        shader.setUniform3f("lightDir", dir, dir, dir);

        //setUpLighting();
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // Clear The Screen And The Depth Buffer
        GL11.glLoadIdentity(); // Reset The View
        GL11.glTranslatef(0.0f, 0.0f, -7.0f); // Move Right And Into The Screen

        Camera.apply();

        Sphere sphere = new Sphere();
        GL11.glColor3f(0.0f,0.4f,0.7f);
        GL11.glTranslatef(0.0f,0,0);
        sphere.draw(1.2f, 32, 64);
        shader.end();

        blinn.begin();
        //GL11.glColor3f(0.0f,0.4f,0.7f);
        GL11.glTranslatef(-2.5f,0,0);
        sphere.draw(1.2f, 32, 64);
        blinn.end();

        checker.begin();
        //GL11.glColor3f(0.4f,0.8f,0.0f);
        GL11.glTranslatef(5.0f,0,0);
        sphere.draw(1.2f, 32, 64);
        checker.end();
    }

    private static void setUpLighting() {
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_LIGHT0);
        GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, BufferTools.asFlippedFloatBuffer(new float[]{0.05f, 0.05f, 0.05f, 1.0f}));
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, BufferTools.asFlippedFloatBuffer(new float[]{4,10, 3, 1}));
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_SPECULAR, BufferTools.asFlippedFloatBuffer(new float[]{0.4f, 0.4f, 0.4f, 1}));
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glColorMaterial(GL11.GL_FRONT, GL11.GL_DIFFUSE);
        GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, 30);
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, BufferTools.asFlippedFloatBuffer(new float[]{0.2f, 0.2f, 0.2f, 1}));
        GL11.glColor3f(0.1f, 0.27f, 0.17f);
    }

    /**
     * Poll Input
     */
    public void pollInput() {
        Camera.acceptInput(getDelta());
        // scroll through key events
        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE)
                    closeRequested = true;
                else if (Keyboard.getEventKey() == Keyboard.KEY_P)
                    snapshot();
            }
        }

        if (Display.isCloseRequested()) {
            closeRequested = true;
        }
    }

    public void snapshot() {
        System.out.println("Taking a snapshot ... snapshot.png");

        GL11.glReadBuffer(GL11.GL_FRONT);

        int width = Display.getDisplayMode().getWidth();
        int height= Display.getDisplayMode().getHeight();
        int bpp = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer );

        File file = new File("snapshot.png"); // The file to save to.
        String format = "PNG"; // Example: "PNG" or "JPG"
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
   
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                int i = (x + (width * y)) * bpp;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
        }
           
        try {
            ImageIO.write(image, format, file);
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    /** 
     * Calculate how many milliseconds have passed 
     * since last frame.
     * 
     * @return milliseconds passed since last frame 
     */
    public int getDelta() {
        long time = (Sys.getTime() * 1000) / Sys.getTimerResolution();
        int delta = (int) (time - lastFrameTime);
        lastFrameTime = time;
     
        return delta;
    }

    private void createWindow() {
        try {
            Display.setDisplayMode(new DisplayMode(1020, 720));
            Display.setVSyncEnabled(true);
            Display.setTitle(windowTitle);
            Display.create();
        } catch (LWJGLException e) {
            Sys.alert("Error", "Initialization failed!\n\n" + e.getMessage());
            System.exit(0);
        }
    }
    
    /**
     * Destroy and clean up resources
     */
    private void cleanup() {
        Display.destroy();
    }
    
    public static void main(String[] args) {
        new PA3().run();
    }
    
    public static class Camera {
        public static float moveSpeed = 0.5f;

        private static float maxLook = 85;

        private static float mouseSensitivity = 0.05f;

        private static Vector3f pos;
        private static Vector3f rotation;

        public static void create() {
            pos = new Vector3f(0, 0, 0);
            rotation = new Vector3f(0, 0, 0);
        }

        public static void apply() {
            if (rotation.y / 360 > 1) {
                rotation.y -= 360;
            } else if (rotation.y / 360 < -1) {
                rotation.y += 360;
            }

            //System.out.println(rotation);
            GL11.glRotatef(rotation.x, 1, 0, 0);
            GL11.glRotatef(rotation.y, 0, 1, 0);
            GL11.glRotatef(rotation.z, 0, 0, 1);
            GL11.glTranslatef(-pos.x, -pos.y, -pos.z);
        }

        public static void acceptInput(float delta) {
            acceptInputRotate(delta);
            acceptInputMove(delta);
        }

        public static void acceptInputRotate(float delta) {
            if (Mouse.isInsideWindow() && Mouse.isButtonDown(0)) {
                float mouseDX = Mouse.getDX();
                float mouseDY = -Mouse.getDY();
                rotation.y += mouseDX * mouseSensitivity * delta;
                rotation.x += mouseDY * mouseSensitivity * delta;
                rotation.x = Math.max(-maxLook, Math.min(maxLook, rotation.x));
            }
        }

        public static void acceptInputMove(float delta) {
            boolean keyUp = Keyboard.isKeyDown(Keyboard.KEY_W);
            boolean keyDown = Keyboard.isKeyDown(Keyboard.KEY_S);
            boolean keyRight = Keyboard.isKeyDown(Keyboard.KEY_D);
            boolean keyLeft = Keyboard.isKeyDown(Keyboard.KEY_A);
            boolean keyFast = Keyboard.isKeyDown(Keyboard.KEY_Q);
            boolean keySlow = Keyboard.isKeyDown(Keyboard.KEY_E);
            boolean keyFlyUp = Keyboard.isKeyDown(Keyboard.KEY_SPACE);
            boolean keyFlyDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);

            float speed;

            if (keyFast) {
                speed = moveSpeed * 5;
            } else if (keySlow) {
                speed = moveSpeed / 2;
            } else {
                speed = moveSpeed;
            }

            speed *= delta;

            if (keyFlyUp) {
                pos.y += speed;
            }
            if (keyFlyDown) {
                pos.y -= speed;
            }

            if (keyDown) {
                pos.x -= Math.sin(Math.toRadians(rotation.y)) * speed;
                pos.z += Math.cos(Math.toRadians(rotation.y)) * speed;
            }
            if (keyUp) {
                pos.x += Math.sin(Math.toRadians(rotation.y)) * speed;
                pos.z -= Math.cos(Math.toRadians(rotation.y)) * speed;
            }
            if (keyLeft) {
                pos.x += Math.sin(Math.toRadians(rotation.y - 90)) * speed;
                pos.z -= Math.cos(Math.toRadians(rotation.y - 90)) * speed;
            }
            if (keyRight) {
                pos.x += Math.sin(Math.toRadians(rotation.y + 90)) * speed;
                pos.z -= Math.cos(Math.toRadians(rotation.y + 90)) * speed;
            }
        }

        public static void setSpeed(float speed) {
            moveSpeed = speed;
        }

        public static void setPos(Vector3f pos) {
            Camera.pos = pos;
        }

        public static Vector3f getPos() {
            return pos;
        }

        public static void setX(float x) {
            pos.x = x;
        }

        public static float getX() {
            return pos.x;
        }

        public static void addToX(float x) {
            pos.x += x;
        }

        public static void setY(float y) {
            pos.y = y;
        }

        public static float getY() {
            return pos.y;
        }

        public static void addToY(float y) {
            pos.y += y;
        }

        public static void setZ(float z) {
            pos.z = z;
        }

        public static float getZ() {
            return pos.z;
        }

        public static void addToZ(float z) {
            pos.z += z;
        }

        public static void setRotation(Vector3f rotation) {
            Camera.rotation = rotation;
        }

        public static Vector3f getRotation() {
            return rotation;
        }

        public static void setRotationX(float x) {
            rotation.x = x;
        }

        public static float getRotationX() {
            return rotation.x;
        }

        public static void addToRotationX(float x) {
            rotation.x += x;
        }

        public static void setRotationY(float y) {
            rotation.y = y;
        }

        public static float getRotationY() {
            return rotation.y;
        }

        public static void addToRotationY(float y) {
            rotation.y += y;
        }

        public static void setRotationZ(float z) {
            rotation.z = z;
        }

        public static float getRotationZ() {
            return rotation.z;
        }

        public static void addToRotationZ(float z) {
            rotation.z += z;
        }

        public static void setMaxLook(float maxLook) {
            Camera.maxLook = maxLook;
        }

        public static float getMaxLook() {
            return maxLook;
        }

        public static void setMouseSensitivity(float mouseSensitivity) {
            Camera.mouseSensitivity = mouseSensitivity;
        }

        public static float getMouseSensitivity() {
            return mouseSensitivity;
        }
    }
}
