/*
 * Copyright 1999-2011 Alibaba Group.
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

import httl.spi.Logger;

public class MultiLogger implements Logger {

	private Logger[] loggers;

	public void setLoggers(Logger[] loggers) {
		this.loggers = loggers;
	}

	public void trace(String msg, Throwable e) {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					logger.trace(msg, e);
				}
			}
		} catch (Throwable t) {
		}
	}

	public void trace(String msg) {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					logger.trace(msg);
				}
			}
		} catch (Throwable t) {
		}
	}

	public void debug(String msg, Throwable e) {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					logger.debug(msg, e);
				}
			}
		} catch (Throwable t) {
		}
	}

	public void debug(String msg) {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					logger.debug(msg);
				}
			}
		} catch (Throwable t) {
		}
	}

	public void info(String msg, Throwable e) {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					logger.info(msg, e);
				}
			}
		} catch (Throwable t) {
		}
	}

	public void info(String msg) {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					logger.info(msg);
				}
			}
		} catch (Throwable t) {
		}
	}

	public void warn(String msg, Throwable e) {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					logger.warn(msg, e);
				}
			}
		} catch (Throwable t) {
		}
	}

	public void warn(String msg) {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					logger.warn(msg);
				}
			}
		} catch (Throwable t) {
		}
	}

	public void error(String msg, Throwable e) {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					logger.error(msg, e);
				}
			}
		} catch (Throwable t) {
		}
	}

	public void error(String msg) {
		try {
			if (loggers != null) {
				for (Logger logger : loggers) {
					logger.error(msg);
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