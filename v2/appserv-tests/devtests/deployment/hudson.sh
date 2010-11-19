if [ -n "$verbose" ]
then
    set -x
fi
export HUDSON=true
export ROOT=`pwd`

REHudson=gf-hudson.us.oracle.com

if [ -x "/usr/bin/cygpath" ]
then
  ROOT=`cygpath -d $ROOT`
  echo "Windows ROOT: $ROOT"
  export CYGWIN=nontsec
fi
rm -rf glassfishv3
wget -q -O revision-under-test.html http://${REHudson}/hudson/job/gf-trunk-build-continuous/lastSuccessfulBuild
grep 'Build #' revision-under-test.html
time wget -q -O glassfish.zip http://${REHudson}/hudson/job/gf-trunk-build-continuous/lastSuccessfulBuild/artifact/bundles/glassfish.zip
unzip -q glassfish.zip
export S1AS_HOME="$ROOT/glassfish3/glassfish"
export APS_HOME="$ROOT/appserv-tests"
cd "$APS_HOME"
(jps |grep Main |cut -f1 -d" " | xargs kill -9  > /dev/null 2>&1) || true
cd "$APS_HOME/devtests/deployment"

antTarget="all-ee"
if [ -z "$DEPL_TARGET"]
then
    $S1AS_HOME/bin/asadmin start-domain
    antTarget="all"
fi

time ant $antTarget

if [ -z "$DEPL_TARGET"]
then
    $S1AS_HOME/bin/asadmin stop-domain
fi
egrep '\[FAILED|UNKNOWN\]' client.log >> /dev/null
if [ $? -eq 0 ]
then
  exit 1
fi
