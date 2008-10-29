/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

import org.apache.tools.ant.BuildException

def resolveFile = { name ->
    return project.resolveFile(project.replaceProperties(name))
}

def getAttribute = { name, defaultValue ->
    def value = attributes[name]
    if (value == null) {
        if (!defaultValue) throw new BuildException("No default for attribute($name)")
        value = defaultValue
    }
    return value
}

def uptodate = { left, right ->
    def uptodateTask = project.createTask('uptodate')
    uptodateTask.srcfile = left
    uptodateTask.targetFile = right
    return uptodateTask.eval()
}

def ant = new AntBuilder(self)
def javacchome = resolveFile('${ofbiz.home.dir}/framework/base/lib/javacc')
def src = getAttribute('src', 'src')
def dir = getAttribute('dir', null)
def file = getAttribute('file', null)
def srcfile = resolveFile("$src/$dir/${file}.jjt")
def dirs = [
    jjtree:     resolveFile(getAttribute('gendir', '${build.dir}/gen-src') + '/jjtree/' + dir),
    javacc:     resolveFile(getAttribute('gendir', '${build.dir}/gen-src') + '/javacc/' + dir),
]
def gen = [
    jjfile:     new File(dirs.jjtree, project.replaceProperties("${file}.jj")),
    javafile:   new File(dirs.javacc, project.replaceProperties("${file}.java")),
]
if (!uptodate(srcfile, gen.jjfile)) {
    ant.delete(dir:dirs.jjtree)
    ant.mkdir(dir:dirs.jjtree)
    ant.jjtree(
        target:             srcfile,
        javacchome:         javacchome,
        outputdirectory:    dirs.jjtree,
    )
}
if (!uptodate(gen.jjfile, gen.javafile)) {
    ant.delete(dir:dirs.javacc)
    ant.mkdir(dir:dirs.javacc)

    ant.javacc(
        target:             gen.jjfile,
        javacchome:         javacchome,
        outputdirectory:    dirs.javacc,
    )
    ant.delete(dir:resolveFile('${build.classes}/' + dir))
}
