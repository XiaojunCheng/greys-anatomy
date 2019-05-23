package com.github.ompc.greys.core.command;

import com.github.ompc.greys.core.Advice;
import com.github.ompc.greys.core.TimeFragment;
import com.github.ompc.greys.core.advisor.listener.AdviceListener;
import com.github.ompc.greys.core.advisor.listener.ReflectAdviceListenerAdapter;
import com.github.ompc.greys.core.command.action.Action;
import com.github.ompc.greys.core.command.action.GetEnhancerAction;
import com.github.ompc.greys.core.command.action.RowAction;
import com.github.ompc.greys.core.command.action.SilentAction;
import com.github.ompc.greys.core.command.annotation.Cmd;
import com.github.ompc.greys.core.command.annotation.IndexArg;
import com.github.ompc.greys.core.command.annotation.NamedArg;
import com.github.ompc.greys.core.command.enhancer.GetEnhancer;
import com.github.ompc.greys.core.command.printer.Printer;
import com.github.ompc.greys.core.exception.ExpressException;
import com.github.ompc.greys.core.manager.TimeFragmentManager;
import com.github.ompc.greys.core.server.Session;
import com.github.ompc.greys.core.textui.TTable;
import com.github.ompc.greys.core.textui.ext.TObject;
import com.github.ompc.greys.core.textui.ext.TTimeFragmentDetail;
import com.github.ompc.greys.core.textui.ext.TTimeFragmentTable;
import com.github.ompc.greys.core.util.*;
import com.github.ompc.greys.core.util.affect.RowAffect;
import com.github.ompc.greys.core.util.matcher.ClassMatcher;
import com.github.ompc.greys.core.util.matcher.GaMethodMatcher;
import com.github.ompc.greys.core.util.matcher.PatternMatcher;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.ompc.greys.core.Advice.newForAfterRetuning;
import static com.github.ompc.greys.core.Advice.newForAfterThrowing;
import static com.github.ompc.greys.core.util.Express.ExpressFactory.newExpress;
import static com.github.ompc.greys.core.util.GaStringUtils.*;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.*;


/**
 * 时光隧道命令<br/>
 * 参数w/d依赖于参数i所传递的记录编号<br/>
 *
 * @author oldmanpushcart@gmail.com
 * @date 14/11/15
 */
@Cmd(name = "tt", sort = 5, summary = "Time Tunnel",
        eg = {
                "tt -t *StringUtils isTop",
                "tt -t *StringUtils isTop params[0].length==1",
                "tt -l",
                "tt -D",
                "tt -i 1000 -w params[0]",
                "tt -i 1000 -d",
                "tt -i 1000"
        })
public class TimeTunnelCommand implements Command {

    /**
     * 时间片段管理
     */
    private final TimeFragmentManager timeFragmentManager = TimeFragmentManager.Factory.getInstance();
    /**
     * TimeTunnel the method call
     */
    @NamedArg(name = "t", summary = "Record the method invocation within time fragments")
    private boolean isTimeTunnel = false;

    @IndexArg(index = 0, isRequired = false, name = "class-pattern", summary = "Path and classname of Pattern Matching")
    private String classPattern;

    @IndexArg(index = 1, isRequired = false, name = "method-pattern", summary = "Method of Pattern Matching")
    private String methodPattern;

    @IndexArg(index = 2, name = "condition-express", isRequired = false,
            summary = "Conditional expression by OGNL",
            description = "" +
                    "FOR EXAMPLE" +
                    "\n" +
                    "     TRUE : 1==1\n" +
                    "     TRUE : true\n" +
                    "    FALSE : false\n" +
                    "     TRUE : params.length>=0\n" +
                    "    FALSE : 1==2\n" +
                    "\n" +
                    "THE STRUCTURE" +
                    "\n" +
                    "          target : the object \n" +
                    "           clazz : the object's class\n" +
                    "          method : the constructor or method\n" +
                    "    params[0..n] : the parameters of method\n" +
                    "       returnObj : the returned object of method\n" +
                    "        throwExp : the throw exception of method\n" +
                    "        isReturn : the method ended by return\n" +
                    "         isThrow : the method ended by throwing exception\n" +
                    "           #cost : the cost(ms) of method"
    )
    private String conditionExpress;

    /**
     * list the TimeTunnel
     */
    @NamedArg(name = "l", summary = "List all the time fragments")
    private boolean isList = false;
    @NamedArg(name = "D", summary = "Delete all the time fragments")
    private boolean isDeleteAll = false;
    /**
     * index of TimeTunnel
     */
    @NamedArg(name = "i", hasValue = true, summary = "Display the detailed information from specified time fragment")
    private Integer index;
    /**
     * expend of TimeTunnel
     */
    @NamedArg(name = "x", hasValue = true, summary = "Expand level of object (0 by default)")
    private Integer expend;
    /**
     * watch the index TimeTunnel
     */
    @NamedArg(name = "w",
            hasValue = true,
            summary = "watch-express, watch the time fragment by OGNL express, like params[0], returnObj, throwExp and so on.",
            description = ""
                    + "FOR EXAMPLE" +
                    "\n" +
                    "    params[0]\n" +
                    "    params[0]+params[1]\n" +
                    "    returnObj\n" +
                    "    throwExp\n" +
                    "    target\n" +
                    "    clazz\n" +
                    "    method\n" +
                    "\n" +
                    "THE STRUCTURE" +
                    "\n" +
                    "          target : the object\n" +
                    "           clazz : the object's class\n" +
                    "          method : the constructor or method\n" +
                    "    params[0..n] : the parameters of method\n" +
                    "       returnObj : the returned object of method\n" +
                    "        throwExp : the throw exception of method\n" +
                    "        isReturn : the method ended by return\n" +
                    "         isThrow : the method ended by throwing exception"
    )
    private String watchExpress = EMPTY;

    @NamedArg(name = "s",
            hasValue = true,
            summary = "Search-expression, to search the time fragments by OGNL express",
            description = "" +
                    "FOR EXAMPLE" +
                    "\n" +
                    "     TRUE : 1==1\n" +
                    "     TRUE : true\n" +
                    "    FALSE : false\n" +
                    "     TRUE : params.length>=0\n" +
                    "    FALSE : 1==2\n" +
                    "\n" +
                    "THE STRUCTURE" +
                    "\n" +
                    "          target : the object \n" +
                    "           clazz : the object's class\n" +
                    "          method : the constructor or method\n" +
                    "    params[0..n] : the parameters of method\n" +
                    "       returnObj : the returned object of method\n" +
                    "        throwExp : the throw exception of method\n" +
                    "        isReturn : the method ended by return\n" +
                    "         isThrow : the method ended by throwing exception\n" +
                    "          #index : the index of time-fragment record\n" +
                    "      #processId : the process ID of time-fragment record\n" +
                    "           #cost : the cost time of time-fragment record"
    )
    private String searchExpress = EMPTY;
    /**
     * play the index TimeTunnel
     */
    @NamedArg(name = "p", summary = "Replay the time fragment specified by index")
    private boolean isPlay = false;
    /**
     * delete the index TimeTunnel
     */
    @NamedArg(name = "d", summary = "Delete time fragment specified by index")
    private boolean isDelete = false;
    @NamedArg(name = "E", summary = "Enable regular expression to match (wildcard matching by default)")
    private boolean isRegEx = false;
    @NamedArg(name = "n", hasValue = true, summary = "Threshold of execution times")
    private Integer threshold;

    /**
     * 检查参数是否合法
     */
    private void checkArguments() {

        // 检查d/p参数是否有i参数配套
        if ((isDelete || isPlay) && null == index) {
            throw new IllegalArgumentException("Time fragment index is expected, please type -i to specify");
        }

        // 在t参数下class-pattern,method-pattern
        if (isTimeTunnel) {
            if (isBlank(classPattern)) {
                throw new IllegalArgumentException("Class-pattern is expected, please type the wildcard expression to match");
            }
            if (isBlank(methodPattern)) {
                throw new IllegalArgumentException("Method-pattern is expected, please type the wildcard expression to match");
            }
        }

        // 一个参数都没有是不行滴
        if (null == index
                && !isTimeTunnel
                && !isDelete
                && !isDeleteAll
                && isBlank(watchExpress)
                && !isList
                && isBlank(searchExpress)
                && !isPlay) {
            throw new IllegalArgumentException("Argument(s) is/are expected, type 'help tt' to read usage");
        }

    }

    /**
     * do the TimeTunnel command
     */
    private GetEnhancerAction doTimeTunnel() {

        return (session, inst, printer) -> new GetEnhancer() {

            private final AtomicInteger timesRef = new AtomicInteger();

            @Override
            public PointCut getPointCut() {
                return new PointCut(
                        new ClassMatcher(new PatternMatcher(isRegEx, classPattern)),
                        new GaMethodMatcher(new PatternMatcher(isRegEx, methodPattern))
                );
            }

            @Override
            public AdviceListener getAdviceListener() {

                return new ReflectAdviceListenerAdapter() {

                    /*
                     * 第一次启动标记
                     */
                    private volatile boolean isFirst = true;

                    private final InvokeCost invokeCost = new InvokeCost();

                    private boolean isOverThreshold(int currentTimes) {
                        return null != threshold
                                && currentTimes >= threshold;
                    }

                    private boolean isInCondition(final Advice advice, long cost) {
                        try {
                            return isBlank(conditionExpress)
                                    || newExpress(advice).bind("cost", cost).is(conditionExpress);
                        } catch (ExpressException e) {
                            return false;
                        }
                    }

                    @Override
                    public void before(Advice advice) {
                        invokeCost.begin();
                    }

                    @Override
                    public void afterFinishing(Advice advice) {

                        final long cost = invokeCost.cost();

                        if (!isInCondition(advice, cost)) {
                            return;
                        }

                        final TimeFragment timeFragment = timeFragmentManager.append(
                                timeFragmentManager.generateProcessId(),
                                advice,
                                new Date(),
                                cost,
                                getStack(getThreadInfo())
                        );

                        final TTimeFragmentTable view = new TTimeFragmentTable(isFirst)
                                // 表格控件不输出表格上边框,这样两个表格就能拼凑在一起
                                .turnOffBottom()
                                // 填充表格内容
                                .add(timeFragment);
                        if (isFirst) {
                            isFirst = false;
                        }

                        final boolean isF = isOverThreshold(timesRef.incrementAndGet());
                        if (isF) {
                            view.turnOnBottom();
                        }
                        printer.print(isF, view.rendering());
                    }

                };
            }
        };

    }

    /**
     * do list timeFragmentMap
     */
    private RowAction doList() {
        return (session, inst, printer) -> {
            final ArrayList<TimeFragment> timeFragments = timeFragmentManager.list();
            printer.print(drawTimeTunnelTable(timeFragments)).finish();
            return new RowAffect(timeFragments.size());
        };

    }

    private boolean hasWatchExpress() {
        return isNotBlank(watchExpress);
    }

    private boolean hasSearchExpress() {
        return isNotBlank(searchExpress);
    }

    private boolean isNeedExpend() {
        return null != expend
                && expend > 0;
    }

    /**
     * do search timeFragmentMap
     */
    private RowAction doSearch() {
        return (session, inst, printer) -> {

            // 匹配的时间片段
            final ArrayList<TimeFragment> matchingTimeFragments = timeFragmentManager.search(searchExpress);

            // 执行watchExpress
            if (hasWatchExpress()) {

                final TTable tTable = new TTable(new TTable.ColumnDefine[]{
                        new TTable.ColumnDefine(TTable.Align.RIGHT),
                        new TTable.ColumnDefine(TTable.Align.LEFT)
                })
                        .padding(1)
                        .addRow("INDEX", "SEARCH-RESULT");

                for (TimeFragment timeFragment : matchingTimeFragments) {
                    final Object value = newExpress(timeFragment.advice).get(watchExpress);
                    tTable.addRow(
                            timeFragment.id,
                            isNeedExpend()
                                    ? new TObject(value, expend).rendering()
                                    : value
                    );

                }

                printer.print(tTable.rendering()).finish();
            } // 单纯的列表格
            else {
                printer.print(drawTimeTunnelTable(matchingTimeFragments)).finish();
            }

            return new RowAffect(matchingTimeFragments.size());
        };

    }

    /**
     * 清除所有的记录
     */
    private RowAction doDeleteAll() {
        return (session, inst, printer) -> {
            final int count = timeFragmentManager.clean();
            printer.println("Time fragments are cleaned.").finish();
            return new RowAffect(count);
        };

    }

    /**
     * 查看记录信息
     */
    private RowAction doWatch() {
        return (session, inst, printer) -> {

            final TimeFragment timeFragment = timeFragmentManager.get(index);
            if (null == timeFragment) {
                printer.println(format("Time fragment[%d] does not exist.", index)).finish();
                return new RowAffect();
            }

            final Advice advice = timeFragment.advice;
            final Object value = newExpress(advice).get(watchExpress);
            if (isNeedExpend()) {
                printer.println(new TObject(value, expend).rendering()).finish();
            } else {
                printer.println(newString(value)).finish();
            }

            return new RowAffect(1);
        };

    }

    /**
     * 重放指定记录
     */
    private RowAction doPlay() {
        return (session, inst, printer) -> {

            final TimeFragment timeFragment = timeFragmentManager.get(index);
            if (null == timeFragment) {
                printer.println(format("Time fragment[%d] does not exist.", index)).finish();
                return new RowAffect();
            }

            final Advice advice = timeFragment.advice;
            final GaMethod method = advice.getMethod();
            final boolean accessible = advice.getMethod().isAccessible();

            final long beginTimestamp = System.currentTimeMillis();
            final long cost;
            Advice reAdvice = null;

            // 注入时间片段id
            PlayIndexHolder.getInstance().set(timeFragment.id);

            try {
                method.setAccessible(true);
                final Object returnObj = method.invoke(advice.target, advice.params);
                reAdvice = newForAfterRetuning(
                        advice.loader,
                        new LazyGet<Class<?>>() {
                            @Override
                            protected Class<?> initialValue() throws Throwable {
                                return advice.getClazz();
                            }
                        },
                        new LazyGet<GaMethod>() {
                            @Override
                            protected GaMethod initialValue() throws Throwable {
                                return advice.getMethod();
                            }
                        },
                        advice.target,
                        advice.params,
                        returnObj
                );
            } catch (Throwable t) {

                // 执行失败:输出失败异常信息
                final Throwable cause;
                if (t instanceof InvocationTargetException) {
                    cause = t.getCause();
                } else {
                    cause = t;
                }

                reAdvice = newForAfterThrowing(
                        advice.loader,
                        new LazyGet<Class<?>>() {
                            @Override
                            protected Class<?> initialValue() throws Throwable {
                                return advice.getClazz();
                            }
                        },
                        new LazyGet<GaMethod>() {
                            @Override
                            protected GaMethod initialValue() throws Throwable {
                                return advice.getMethod();
                            }
                        },
                        advice.target,
                        advice.params,
                        cause
                );

            } finally {
                method.setAccessible(accessible);
                cost = System.currentTimeMillis() - beginTimestamp;

                // 清除时间片段id
                // PlayIndexHolder.getInstance().remove();
            }

            final TimeFragment reTimeFragment = new TimeFragment(
                    timeFragment.id,
                    timeFragment.processId,
                    reAdvice,
                    timeFragment.gmtCreate,
                    cost,
                    timeFragment.stack
            );


            final TTimeFragmentDetail view = new TTimeFragmentDetail(inst, reTimeFragment, expend);
            printer.print(view.rendering())
                    .println(format("Time fragment[%d] successfully replayed.", index))
                    .finish();
            return new RowAffect(1);
        };
    }

    /**
     * 删除指定记录
     */
    private RowAction doDelete() {

        return new RowAction() {
            @Override
            public RowAffect action(Session session, Instrumentation inst, Printer printer) throws Throwable {
                final RowAffect affect = new RowAffect();
                if (timeFragmentManager.delete(index) != null) {
                    affect.rCnt(1);
                }
                printer.println(format("Time fragment[%d] successfully deleted.", index)).finish();
                return affect;
            }
        };

    }

    /**
     * 绘制TimeTunnel表格
     */
    private String drawTimeTunnelTable(final ArrayList<TimeFragment> timeFragments) {
        final TTimeFragmentTable view = new TTimeFragmentTable(true);
        for (TimeFragment timeFragment : timeFragments) {
            view.add(timeFragment);
        }
        return view.rendering();
    }


    /**
     * 展示指定记录
     */
    private RowAction doShow() {
        return (session, inst, printer) -> {

            final TimeFragment timeFragment = timeFragmentManager.get(index);
            if (null == timeFragment) {
                printer.println(format("Time fragment[%d] does not exist.", index)).finish();
                return new RowAffect();
            }

            printer.print(new TTimeFragmentDetail(inst, timeFragment, expend).rendering()).finish();
            return new RowAffect(1);
        };
    }

    @Override
    public Action getAction() {

        // 检查参数
        checkArguments();

        final Action action;
        if (isTimeTunnel) {
            action = doTimeTunnel();
        } else if (isList) {
            action = doList();
        } else if (isDeleteAll) {
            action = doDeleteAll();
        } else if (isDelete) {
            action = doDelete();
        } else if (isPlay) {
            action = doPlay();
        } else if (null != index) {
            if (hasWatchExpress()) {
                action = doWatch();
            } else {
                action = doShow();
            }
        } else if (hasSearchExpress()) {
            action = doSearch();
        } else {
            action = (SilentAction) (session, inst, printer) -> {
                throw new UnsupportedOperationException("not support operation.");
            };
        }

        return action;
    }

}
