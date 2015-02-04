/*
 * License (MIT)
 *
 * Copyright (c) 2014-2015 Granite Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.granitepowered.granite.bytecode.classes;

import static org.granitepowered.granite.util.MinecraftUtils.wrap;

import javassist.CtClass;
import org.granitepowered.granite.bytecode.BytecodeClass;
import org.granitepowered.granite.bytecode.Proxy;
import org.granitepowered.granite.bytecode.ProxyCallbackInfo;
import org.granitepowered.granite.impl.entity.projectile.GraniteEgg;
import org.granitepowered.granite.mappings.Mappings;
import org.granitepowered.granite.mc.MCEntityEgg;
import org.granitepowered.granite.mc.MCInterface;

public class EntityEggClass extends BytecodeClass {

    public EntityEggClass() {
        super("EntityEgg");

        int idx = addParameter("onImpact", CtClass.floatType);

        addArgumentsVariable("onImpact");

        replaceMethodCallParameter("onImpact", Mappings.getCtMethod("Entity", "attackEntityFrom"), 1, "$mArgs[" + idx + "]");
    }

    @Proxy(methodName = "onImpact")
    public Object onImpact(ProxyCallbackInfo<MCEntityEgg> info) throws Throwable {
        float damage = (float) ((GraniteEgg) wrap(info.getCaller())).getDamage();
        return info.callback(info.getArguments(), damage);
    }
}