package confictura.util;

import rhino.*;
import rhino.module.*;
import rhino.module.provider.*;

import java.io.*;
import java.net.*;

import static arc.Core.*;
import static confictura.ConficturaMod.*;
import static mindustry.Vars.*;

/**
 * Common utility bridge for JavaScript codes.
 * @author GlennFolker
 */
public final class ScriptUtils{
    public static Context context;
    public static ImporterTopLevel vanillaScope, scope;

    private ScriptUtils(){
        throw new AssertionError();
    }

    /** Initializes the utility bridge. Main-thread only! */
    public static void init(){
        var scripts = mods.getScripts();

        context = scripts.context;
        vanillaScope = (ImporterTopLevel)scripts.scope;

        scope = new ImporterTopLevel(context);
        context.evaluateString(scope, files.internal("scripts/global.js").readString(), "global.js", 1);
        new RequireBuilder()
            .setModuleScriptProvider(new SoftCachingModuleScriptProvider(new SectorScriptsProvider()))
            .setSandboxed(false).createRequire(context, scope).install(scope);
    }

    /**
     * Imports packages defined by this mod into the script scope.
     * @param scope {@link #vanillaScope} for the base game's script scope (mod scripts folder and console), or
     *              {@link #scope} for scope specifically used by this mod.
     */
    public static void importDefaults(ImporterTopLevel scope){
        for(var name : packages) importPackage(scope, name);
    }

    /**
     * Imports a single package to the given scope.
     * @param scope See {@link #importDefaults(ImporterTopLevel)}.
     * @param name  The package's fully qualified name.
     */
    public static void importPackage(ImporterTopLevel scope, String name){
        var p = new NativeJavaPackage(name, mods.mainLoader());
        p.setParentScope(scope);

        scope.importPackage(p);
    }

    public static class SectorScriptsProvider extends UrlModuleSourceProvider{
        public SectorScriptsProvider(){
            super(null, null);
        }

        @Override
        public ModuleSource loadSource(String moduleId, Scriptable paths, Object validator) throws IOException, URISyntaxException{
            String base = "sector-scripts/confictura", path = base + "/" + moduleId;
            var file = tree.get(path);
            if(!file.exists()) throw new IOException("Script `" + path + "` doesn't exist.");

            return new ModuleSource(file.reader(), new URI(path), tree.get(base).file().toURI(), validator);
        }
    }
}
