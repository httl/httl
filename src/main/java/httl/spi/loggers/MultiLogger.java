/*
 * Copyright 2011-2013 HTTL Team.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package httl.spi.loggers;

import httl.internal.util.Version;
import httl.spi.Logger;

/**
 * MultiLogger. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.engines.DefaultEngine#setLogger(Logger)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class MultiLogger implements Logger {

	private Logger[] loggers;

	/**
	 * httl.properties: loggers=httl.spi.loggers.Log4jLogger
	 */
	public void setLoggers(Logger[] loggers) {
		this.loggers = loggers;
	}

	public void init() {
		if (loggers == null) {
			try {
				setLoggers(new Logger[] { new Slf4jLogger() });
			} catch (Throwable e1) {
				try {
					setLoggers(new Logger[] { new JclLogger() });
				} catch (Throwable e2) {
					try {
						setLoggers(new Logger[] { new Log4jLogger() });
					} catch (Throwable e3) {
						try {
							setLoggers(new Logger[] { new JdkLogger() });
						} catch (Throwable e4) {
							setLoggers(new Logger[] { new SimpleLogger() });
						}
					}
				}
			}
		}
	}

	private String appendEnvInfo(Throwable e) {
		return appendEnvInfo(e == null ? "" : e.getMessage());
	}

	private String appendEnvInfo(String msg) {
		return msg + ", httl: " + Version.getVersion() + ", jvm: " + System.getProperty("java.version")
				+ ", os: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch");
	}

	public void trace(String msg, Throwable e) {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					logger.trace(appendEnvInfo(msg), e);
				}
			}
		} catch (Throwable t) {
		}
	}

	public void trace(Throwable e) {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					logger.trace(appendEnvInfo(e), e);
				}
			}
		} catch (Throwable t) {
		}
	}

	public void trace(String msg) {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					logger.trace(appendEnvInfo(msg));
				}
			}
		} catch (Throwable t) {
		}
	}

	public void debug(String msg, Throwable e) {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					logger.debug(appendEnvInfo(msg), e);
				}
			}
		} catch (Throwable t) {
		}
	}

	public void debug(Throwable e) {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					logger.debug(appendEnvInfo(e), e);
				}
			}
		} catch (Throwable t) {
		}
	}

	public void debug(String msg) {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					logger.debug(appendEnvInfo(msg));
				}
			}
		} catch (Throwable t) {
		}
	}

	public void info(String msg, Throwable e) {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					logger.info(appendEnvInfo(msg), e);
				}
			}
		} catch (Throwable t) {
		}
	}

	public void info(Throwable e) {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					logger.info(appendEnvInfo(e), e);
				}
			}
		} catch (Throwable t) {
		}
	}

	public void info(String msg) {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					logger.info(appendEnvInfo(msg));
				}
			}
		} catch (Throwable t) {
		}
	}

	public void warn(String msg, Throwable e) {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					logger.warn(appendEnvInfo(msg), e);
				}
			}
		} catch (Throwable t) {
		}
	}

	public void warn(Throwable e) {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					logger.warn(appendEnvInfo(e), e);
				}
			}
		} catch (Throwable t) {
		}
	}

	public void warn(String msg) {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					logger.warn(appendEnvInfo(msg));
				}
			}
		} catch (Throwable t) {
		}
	}

	public void error(String msg, Throwable e) {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					logger.error(appendEnvInfo(msg), e);
				}
			}
		} catch (Throwable t) {
		}
	}

	public void error(Throwable e) {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					logger.error(appendEnvInfo(e), e);
				}
			}
		} catch (Throwable t) {
		}
	}

	public void error(String msg) {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					logger.error(appendEnvInfo(msg));
				}
			}
		} catch (Throwable t) {
		}
	}

	public boolean isTraceEnabled() {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					if (logger.isTraceEnabled()) {
						return true;
					}
				}
			}
			return false;
		} catch (Throwable t) {
			return false;
		}
	}

	public boolean isDebugEnabled() {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					if (logger.isDebugEnabled()) {
						return true;
					}
				}
			}
			return false;
		} catch (Throwable t) {
			return false;
		}
	}

	public boolean isInfoEnabled() {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					if (logger.isInfoEnabled()) {
						return true;
					}
				}
			}
			return false;
		} catch (Throwable t) {
			return false;
		}
	}

	public boolean isWarnEnabled() {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					if (logger.isWarnEnabled()) {
						return true;
					}
				}
			}
			return false;
		} catch (Throwable t) {
			return false;
		}
	}

	public boolean isErrorEnabled() {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					if (logger.isErrorEnabled()) {
						return true;
					}
				}
			}
			return false;
		} catch (Throwable t) {
			return false;
		}
	}

}