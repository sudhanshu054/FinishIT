import { useEffect, useMemo, useState } from "react";
import { api } from "../api/client";

const statuses = ["TODO", "IN_PROGRESS", "DONE"];

export default function DashboardPage() {
  const session = useMemo(() => JSON.parse(localStorage.getItem("user") || "{}"), []);
  const isAdmin = session.role === "ADMIN";

  const [dashboard, setDashboard] = useState({});
  const [tasks, setTasks] = useState([]);
  const [projects, setProjects] = useState([]);
  const [members, setMembers] = useState([]);
  const [projectForm, setProjectForm] = useState({ name: "", description: "" });
  const [query, setQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState("ACTIVE");
  const [form, setForm] = useState({
    projectId: "",
    memberId: "",
    title: "",
    description: "",
    deadline: ""
  });

  const load = async () => {
    const [d, t, p] = await Promise.all([
      api.get("/tasks/dashboard"),
      api.get("/tasks"),
      api.get("/projects")
    ]);
    setDashboard(d.data);
    setTasks(t.data);
    setProjects(p.data);
    if (isAdmin) {
      const memberRes = await api.get("/tasks/members");
      setMembers(memberRes.data);
    }
  };

  useEffect(() => {
    load();
    const id = setInterval(load, 7000);
    return () => clearInterval(id);
  }, []);

  const createTask = async (e) => {
    e.preventDefault();
    await api.post("/tasks", form);
    setForm({ projectId: "", memberId: "", title: "", description: "", deadline: "" });
    load();
  };

  const createProject = async (e) => {
    e.preventDefault();
    await api.post("/projects", projectForm);
    setProjectForm({ name: "", description: "" });
    load();
  };

  const updateStatus = async (taskId, status) => {
    await api.patch(`/tasks/${taskId}/status`, { status });
    load();
  };

  const logout = () => {
    localStorage.clear();
    window.location.href = "/auth";
  };

  const filteredTasks = useMemo(() => {
    const queryLower = query.trim().toLowerCase();
    const searchingDone = queryLower.includes("done");

    return tasks.filter((task) => {
      const byStatus =
        statusFilter === "ALL" ||
        task.status === statusFilter ||
        (statusFilter === "ACTIVE" && task.status !== "DONE") ||
        (statusFilter === "ACTIVE" && searchingDone);
      const byQuery =
        queryLower === "" ||
        task.title.toLowerCase().includes(queryLower) ||
        task.projectName.toLowerCase().includes(queryLower) ||
        task.assignedToEmail.toLowerCase().includes(queryLower) ||
        task.status.toLowerCase().includes(queryLower.replace(" ", "_"));

      return byStatus && byQuery;
    });
  }, [tasks, query, statusFilter]);

  const groupedCounts = useMemo(() => {
    return {
      TODO: filteredTasks.filter((t) => t.status === "TODO").length,
      IN_PROGRESS: filteredTasks.filter((t) => t.status === "IN_PROGRESS").length,
      DONE: filteredTasks.filter((t) => t.status === "DONE").length
    };
  }, [filteredTasks]);

  return (
    <div className={`dash-wrap ${isAdmin ? "admin-theme" : "member-theme"}`}>
      <header className="top-nav">
        <div className="title-block">
          <h2>{session.organization} Workspace</h2>
          <p>{isAdmin ? "Admin control center" : "Member execution hub"} · {session.email}</p>
        </div>
        <button className="ghost-btn" onClick={logout}>Logout</button>
      </header>

      <section className="cards">
        <Card title="Total Tasks" value={dashboard.total || 0} tone="indigo" />
        <Card title="Todo" value={dashboard.todo || 0} tone="blue" />
        <Card title="In Progress" value={dashboard.inProgress || 0} tone="amber" />
        <Card title="Done" value={dashboard.done || 0} tone="green" />
        <Card title="Overdue" value={dashboard.overdue || 0} tone="red" />
      </section>

      {isAdmin && (
        <section className="panel glass">
          <h3>Project Studio</h3>
          <form onSubmit={createProject} className="task-form">
            <input
              value={projectForm.name}
              onChange={(e) => setProjectForm({ ...projectForm, name: e.target.value })}
              placeholder="Project name"
              required
            />
            <input
              value={projectForm.description}
              onChange={(e) => setProjectForm({ ...projectForm, description: e.target.value })}
              placeholder="Project description"
            />
            <button type="submit" className="primary-btn">Create Project</button>
          </form>
        </section>
      )}

      {isAdmin && (
        <section className="panel glass">
          <h3>Task Command Center</h3>
          <form onSubmit={createTask} className="task-form task-grid">
            <select value={form.projectId} onChange={(e) => setForm({ ...form, projectId: Number(e.target.value) })} required>
              <option value="">Select project</option>
              {projects.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
            </select>
            <select value={form.memberId} onChange={(e) => setForm({ ...form, memberId: Number(e.target.value) })} required>
              <option value="">Assign member</option>
              {members.map((m) => <option key={m.id} value={m.id}>{m.email}</option>)}
            </select>
            <input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} placeholder="Task title" required />
            <input value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} placeholder="Task details" />
            <input type="datetime-local" value={form.deadline} onChange={(e) => setForm({ ...form, deadline: e.target.value })} required />
            <button type="submit" className="primary-btn assign-btn">Assign Task</button>
          </form>
        </section>
      )}

      <section className="panel glass">
        <div className="toolbar">
          <h3>{isAdmin ? "Organization Task Stream" : "My Smart Task Stream"}</h3>
          <div className="toolbar-controls">
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Search task, project, assignee (type done to view completed)"
            />
            <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
              <option value="ACTIVE">Active tasks (default)</option>
              <option value="ALL">All statuses</option>
              {statuses.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>
        </div>
        <div className="micro-stats">
          <span>Todo: {groupedCounts.TODO}</span>
          <span>In Progress: {groupedCounts.IN_PROGRESS}</span>
          <span>Done: {groupedCounts.DONE}</span>
          <span>Showing: {filteredTasks.length}</span>
        </div>
        <div className="task-list">
          {filteredTasks.map((t) => (
            <article key={t.id} className="task-item modern">
              <div>
                <h4>{t.title}</h4>
                <p>{t.projectName} · deadline: {new Date(t.deadline).toLocaleString()}</p>
                <small>Assigned: {t.assignedToEmail}</small>
              </div>
              <div className="task-side">
                <span className={`badge ${badgeClass(t.status)}`}>{t.status.replace("_", " ")}</span>
                {!isAdmin && (
                  <select value={t.status} onChange={(e) => updateStatus(t.id, e.target.value)}>
                    {statuses.map((s) => <option key={s} value={s}>{s}</option>)}
                  </select>
                )}
              </div>
            </article>
          ))}
          {filteredTasks.length === 0 && (
            <div className="empty-state">No tasks match this filter. Try changing status or search.</div>
          )}
        </div>
      </section>

      {isAdmin && (
        <section className="panel glass">
          <h3>Member Load Insights</h3>
          <div className="member-grid">
            {members.map((member) => (
              <div className="member-card" key={member.id}>
                <strong>{member.email}</strong>
                <p>Assigned tasks: {dashboard.progressByMember?.[member.id] || 0}</p>
              </div>
            ))}
          </div>
        </section>
      )}
    </div>
  );
}

function Card({ title, value, tone }) {
  return (
    <div className={`card tone-${tone}`}>
      <h4>{title}</h4>
      <p>{value}</p>
    </div>
  );
}

function badgeClass(status) {
  if (status === "DONE") return "badge-green";
  if (status === "IN_PROGRESS") return "badge-amber";
  return "badge-blue";
}
