<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:homeLayout>
	<jsp:attribute name="title">
		Home
	</jsp:attribute>
	<jsp:body>
		<div class="container">
			<div class="row">
				<div class="col-md-4" style="margin-top:0.35%;">
					<div class="card">
						<div class="card-header">Login</div>
						<div class="card-body">
							<div class="alert alert-danger" id="message" role="alert" style="display: none"></div>
							<form action="/coop/login" method ="post">
								<div class="form-group">
									<label for="username">Username</label>
									<input autofocus type="text" class="form-control" name="username" id="username" placeholder="Username" required>
								</div>
								<div class="form-group">
									<label for="password">Password</label>
									<input type="password" class="form-control" id="password" name="password" placeholder="Password" required>
								</div>
								<input type="submit" value="Login" class="btn submit-btn">
								<a href="/coop/user/add" class="btn other-btn">Register</a>
							</form>
						</div>
					</div>
				</div>
				<div class="col-md-8">
					<div class="card">
						<div class="card-header">Co-Op</div>
						<div class="card-body">
							<div id="homeCarousel" class="carousel slide" data-ride="carousel">
								<!-- Indicators -->
								<ul class="carousel-indicators">
									<li data-target="#homeCarousel" data-slide-to="0" class="active"></li>
									<li data-target="#homeCarousel" data-slide-to="1"></li>
									<li data-target="#homeCarousel" data-slide-to="2"></li>
								</ul>

								<!-- The slideshow -->
								<div class="carousel-inner">
									<div class="carousel-item active">
										<img class="img-fluid" src="<c:url value='/resources/images/coop-model.png'/>" alt="Co-Op model">
										<div style="background-color:#FFFFFF;padding-top:25px;">
											Co-Op is a software maintenance-focused process and supporting toolset designed for use in academic environments.
											The toolset focuses on impact analysis, use of version control systems that adheres to the process, and communication amongst part-time developers.
										</div>
									</div>
									<div class="carousel-item">
										<div class="row">
											<div class="col-md-4 my-auto">
												<img class="img-fluid" src="<c:url value='/resources/images/gitlab.png'/>" alt="gitlab icon">
											</div>
											<div class="col-md-4 my-auto">
												<img class="img-fluid" src="<c:url value='/resources/images/octocat.png'/>" alt="github icon">
											</div>
											<div class="col-md-4 my-auto">
												<img class="img-fluid" src="<c:url value='/resources/images/slack.png'/>" alt="slack icon">
											</div>
										</div>
										<div style="background-color:#FFFFFF;padding-top:25px;">
											Co-Op integrates with remote repositories hosted on either GitHub or a GitLab instance to help enforce repository-related process constraints.
											It also integrates with Slack, supporting the work of students as part-time developers.
										</div>
									</div>
									<div class="carousel-item">
										<img class="img-fluid" src="<c:url value='/resources/images/coop-project-data.png'/>" alt="Co-Op project data">
										<div style="background-color:#FFFFFF;padding-top:25px;">
											Co-Op aggregates data for each user, project, and cycle. It generates reports that include:
											<ol>
												<li>How much time each user reported working on tasks as owners, helpers, and reviewers</li>
												<li>How much time each user reported working on specific task categories like feature implementation, bug fix, etc.</li>
											</ol>
										</div>
									</div>
								</div>

								<!-- Left and right controls -->
								<a class="carousel-control-prev" href="#homeCarousel" data-slide="prev">
									<span class="carousel-control-prev-icon"></span>
								</a>
								<a class="carousel-control-next" href="#homeCarousel" data-slide="next">
									<span class="carousel-control-next-icon"></span>
								</a>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
    </jsp:body>
</t:homeLayout>